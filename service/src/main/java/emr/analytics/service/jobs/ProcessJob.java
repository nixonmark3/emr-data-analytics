package emr.analytics.service.jobs;

import emr.analytics.models.messages.JobRequest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public abstract class ProcessJob extends AnalyticsJob {

    protected String path = "../algorithms";
    protected String command;
    protected String commandPrefix;
    protected List<String> commandArguments;

    public ProcessJob(JobRequest request, String command, String commandPrefix){
        this(request, command, commandPrefix, new ArrayList<String>());
    }

    public ProcessJob(JobRequest request, String command, String commandPrefix, List<String> arguments){
        super(request);
        this.command = command;
        this.commandPrefix = commandPrefix;
        this.commandArguments = arguments;
    }

    protected String getFileName(){
        return String.format("%s/%s.py", this.path, this.id.toString());
    }

    public ProcessBuilder getProcess(){

        this.writeSource();

        ProcessBuilder builder = new ProcessBuilder();

        List<String> processArgs = this.processArguments();

        // build process command
        String cmd = this.command;
        if (this.commandPrefix != null){

            Map<String, String> env = builder.environment();;
            if (env.containsKey(this.commandPrefix)) {
                String commandPrefix = env.get(this.commandPrefix);

                for (int i = 0; i < processArgs.size(); i++)
                    processArgs.set(i, processArgs.get(i).replace("$" + this.commandPrefix, commandPrefix));

                cmd = String.format("%s/%s", commandPrefix, this.command);
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
            out.write(this.source);
            out.close();
        }
        catch(IOException ex) {
            System.err.println("IO Exception occurred.");
        }
    }
}
