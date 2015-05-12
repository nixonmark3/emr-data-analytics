package emr.analytics.service.jobs;

import emr.analytics.models.diagram.Diagram;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public abstract class ProcessJob extends AnalyticsJob {

    protected String _path = "../algorithms";
    protected String _command;
    protected String _commandPrefix;
    protected List<String> _commandArguments;

    public ProcessJob(UUID id, JobMode mode, String template, String command, String commandPrefix, Diagram diagram){
        this(id, mode, template, command, commandPrefix, diagram, new ArrayList<String>());
    }

    public ProcessJob(UUID id, JobMode mode, String template, String command, String commandPrefix, Diagram diagram, List<String> arguments){
        super(id, mode, template, diagram);
        this._command = command;
        this._commandPrefix = commandPrefix;
        this._commandArguments = arguments;
    }

    protected String getFileName(){
        return String.format("%s/%s.py",
                this._path,
                this._id.toString());
    }

    public ProcessBuilder getProcess(){

        this.writeSource();

        ProcessBuilder builder = new ProcessBuilder();

        List<String> processArgs = this.processArguments();

        // build process command
        String cmd = this._command;
        if (this._commandPrefix != null){

            Map<String, String> env = builder.environment();;
            if (env.containsKey(this._commandPrefix)) {
                String commandPrefix = env.get(this._commandPrefix);

                for (int i = 0; i < processArgs.size(); i++)
                    processArgs.set(i, processArgs.get(i).replace("$" + this._commandPrefix, commandPrefix));

                cmd = String.format("%s/%s", commandPrefix, this._command);
            }
        }

        // prepend the command name to the head of the list
        processArgs.add(0, cmd);

        builder.command(processArgs);

        return builder;
    }

    protected abstract List<String> processArguments();

    protected class ProcessArgumentBuilder {

        List<String> arguments = new ArrayList<String>();

        public void add(String value){
            arguments.add(value);
        }

        public void addAll(List<String> values){
            arguments.addAll(values);
        }

        public void addKeyValue(String key, String value){
            arguments.add(key);
            arguments.add(value);
        }

        public void addOption(String key, Optional<String> value){
            if (value.isPresent()){
                arguments.add(key);
                arguments.add(value.get());
            }
        }

        public void addOptionList(String key, List<String> values) {
            if (!values.isEmpty()) {
                arguments.add(key);
                arguments.add(values.stream().reduce((x, y) -> x + "," + y).get());
            }
        }

        public List<String> get(){
            return arguments;
        }
    }

    public void removeSource(){

        String fileName = this.getFileName();
        try {
            Files.delete(Paths.get(fileName));
        }
        catch(IOException ex) {
            System.err.println("IO Exception occurred.");
        }
    }

    private void writeSource(){

        String fileName = this.getFileName();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(this._source);
            out.close();
        }
        catch(IOException ex) {
            System.err.println("IO Exception occurred.");
        }
    }
}
