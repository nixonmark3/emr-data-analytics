package emr.analytics.service.interpreters;

import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.IOUtils;
import py4j.GatewayServer;

import java.io.*;
import java.net.ServerSocket;
import java.util.Map;

/**
 *
 */
public class PythonInterpreter extends Interpreter implements ExecuteResultHandler {

    // py4j gateway server - allows python script to communicate with this java class
    protected GatewayServer gatewayServer;

    // apache common class used to fork and execute another process
    private DefaultExecutor executor;

    // define streams for output and errors
    private InterpreterOutputStream outputStream;
    private InterpreterOutputStream errorStream;

    // tracks the many state of the python process
    protected InterpreterFlags flags;

    // The current interpreter request
    private InterpreterRequest interpreterRequest = null;
    private String interpreterOutput;

    // locks used to synchronize the python and java processes
    private final Integer scriptInitializedNotifier = 0;
    private final Integer statementReceivedNotifier = 0;
    private final Integer statementCompletedNotifier = 0;

    public PythonInterpreter(InterpreterNotificationHandler notificationHandler){
        super(notificationHandler);

        // load python properties
        loadProperties("python");

        // initialize flags
        flags = new InterpreterFlags();
    }

    /**
     *
     */
    @Override
    public void start(){

        // retrieve the pyspark source from resources and create python file
        createScriptFile();

        int port = getOpenPort();
        gatewayServer = new GatewayServer(this, port);
        gatewayServer.start();

        // configure python command line
        CommandLine cmd = getCommandLine();

        // setup executor and stream handler
        executor = new DefaultExecutor();
        executor.setWorkingDirectory(this.getWorkingDirectory());

        // instantiate the output and error streams
        outputStream = new InterpreterOutputStream(this.notificationHandler, InterpreterOutputStream.OutputLevel.OUT);
        errorStream = new InterpreterOutputStream(this.notificationHandler, InterpreterOutputStream.OutputLevel.ERROR);

        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
        executor.setStreamHandler(streamHandler);
        executor.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));

        try {
            Map<String, String> env = EnvironmentUtils.getProcEnvironment();
            executor.execute(cmd, env, this);
            flags.setFlag(InterpreterFlags.InterpreterFlag.STARTED);
        }
        catch (IOException e) {
            throw new InterpreterException(e);
        }
    }

    /**
     *
     */
    @Override
    public void stop(){
        executor.getWatchdog().destroyProcess();
        gatewayServer.shutdown();
    }

    /**
     *
     * @param statements
     * @return
     */
    @Override
    public InterpreterResult interpret(String statements) {

        // confirm the python script has been started
        if (!flags.hasFlag(InterpreterFlags.InterpreterFlag.STARTED)) {

            return new InterpreterResult(InterpreterResult.State.FAILURE,
                    String.format("The python process is not running. Additional info: %s.",
                            outputStream.toString()));
        }

        // confirm the script has been initialized
        int scriptInitializerWait = 10;  // wait a maximum of 10 seconds
        synchronized (scriptInitializedNotifier) {
            long startTime = System.currentTimeMillis();

            while (!flags.hasFlag(InterpreterFlags.InterpreterFlag.INITIALIZED)
                    && flags.hasFlag(InterpreterFlags.InterpreterFlag.STARTED)
                    && System.currentTimeMillis() - startTime < scriptInitializerWait * 1000) {

                try { scriptInitializedNotifier.wait(1000); }
                catch (InterruptedException e) {
                    // do nothing
                }
            }
        }

        if (!flags.hasFlag(InterpreterFlags.InterpreterFlag.STARTED)) {
            // python script failed to initialize and has been terminated
            return new InterpreterResult(InterpreterResult.State.FAILURE,
                    String.format("Failed to start python process. Additional info: %s.",
                            outputStream.toString()));
        }
        if (!flags.hasFlag(InterpreterFlags.InterpreterFlag.INITIALIZED)) {
            // timeout. didn't get initialized message
            return new InterpreterResult(InterpreterResult.State.FAILURE,
                    String.format("The python process is not responding. Additional info: %s.",
                            outputStream.toString()));
        }

        // todo: what if the interpreting is already interpreting ?

        // create the request and set the run flags
        interpreterRequest = new InterpreterRequest(statements);
        flags.setFlag(InterpreterFlags.InterpreterFlag.RUNNING);
        flags.clearFlag(InterpreterFlags.InterpreterFlag.FAILED);
        interpreterOutput = "";

        // notify the waiting script thread that a statement has been set
        synchronized (statementReceivedNotifier) {
            statementReceivedNotifier.notify();
        }

        // wait for the statement to be completed
        synchronized (statementCompletedNotifier) {
            while (flags.hasFlag(InterpreterFlags.InterpreterFlag.RUNNING)) {
                try {
                    statementCompletedNotifier.wait(1000);
                }
                catch (InterruptedException e) {
                    // do nothing


                }
            }
        }

        if (flags.hasFlag(InterpreterFlags.InterpreterFlag.FAILED)) {
            return new InterpreterResult(InterpreterResult.State.FAILURE, interpreterOutput);
        } else {
            return new InterpreterResult(InterpreterResult.State.SUCCESS, interpreterOutput);
        }
    }

    /**
     * Construct and save the script file
     */
    private void createScriptFile(){

        File out = new File(this.getScriptPath());

        if (out.exists() && out.isDirectory()) {
            throw new InterpreterException("Unable to create python script " + out.getAbsolutePath());
        }

        try (FileOutputStream outStream = new FileOutputStream(out);) {
            for (String scriptFile : this.scriptFiles()){
                IOUtils.copy(
                        getClass().getClassLoader().getResourceAsStream(String.format("python/%s.py", scriptFile)),
                        outStream);
            }
        }
        catch (IOException ex) {
            throw new InterpreterException(ex);
        }
    }

    /**
     *
     * @return
     */
    protected String[] scriptFiles(){
        return new String[] { "python_init", "python_eval" };
    }

    /**
     *
     * @return
     */
    protected CommandLine getCommandLine(){

        String cmd = getProperties().getProperty("python.command");

        CommandLine commandLine = CommandLine.parse(cmd);
        commandLine.addArgument(this.getScriptPath(), false);
        commandLine.addArgument(Integer.toString(gatewayServer.getPort()), false);

        return commandLine;
    }

    /**
     *
     * @return
     */
    protected String getScriptPath(){
        return String.format("%s/%s.py",
                this.getWorkingDirectory().getAbsolutePath(),
                this.getClass().getName());
    }

    /**
     *
     * @return
     */
    protected File getWorkingDirectory(){
        return new File(this.getProperties().getProperty("python.workingDir"));
    }

    /**
     *
     */
    public void onPythonScriptInitialized() {

        synchronized (scriptInitializedNotifier) {
            flags.setFlag(InterpreterFlags.InterpreterFlag.INITIALIZED);
            scriptInitializedNotifier.notifyAll();
        }
    }

    /**
     *
     * @return
     */
    public InterpreterRequest getStatements() {

        synchronized (statementReceivedNotifier) {
            while (interpreterRequest == null) {
                try {
                    statementReceivedNotifier.wait(1000);
                }
                catch (InterruptedException e) {
                    // do nothing
                }
            }
            InterpreterRequest request = interpreterRequest;
            interpreterRequest = null;
            return request;
        }
    }

    public void setStatementsComplete() {

        synchronized (statementCompletedNotifier) {
            flags.clearFlag(InterpreterFlags.InterpreterFlag.RUNNING);
            statementCompletedNotifier.notify();
        }

    }

    public void setStatementsFailed(String message) {

        synchronized (statementCompletedNotifier) {
            flags.clearFlag(InterpreterFlags.InterpreterFlag.RUNNING);
            flags.setFlag(InterpreterFlags.InterpreterFlag.FAILED);
            interpreterOutput = message;
            statementCompletedNotifier.notify();
        }
    }

    @Override
    public void onProcessComplete(int exitValue) {

        flags.clearFlag(InterpreterFlags.InterpreterFlag.STARTED);
    }

    @Override
    public void onProcessFailed(ExecuteException e) {

        flags.clearFlag(InterpreterFlags.InterpreterFlag.STARTED);
    }

    private int getOpenPort() {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
            socket.close();
        } catch (IOException e) {
            throw new InterpreterException(e);
        }
        return port;
    }
}
