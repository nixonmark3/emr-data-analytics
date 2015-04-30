package emr.analytics.service.processes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AnalyticsProcessBuilder {

    private String _fileName;
    private ProcessBuilder _builder = new ProcessBuilder();

    public AnalyticsProcessBuilder(String fileName, String command){
        this(fileName, command, new ArrayList<String>(), null);
    }

    public AnalyticsProcessBuilder(String fileName, String command, List<String> arguments){
        this(fileName, command, arguments, null);
    }

    public AnalyticsProcessBuilder(String fileName, String command, String evCommandPrefix){
        this(fileName, command, new ArrayList<String>(), evCommandPrefix);
    }

    public AnalyticsProcessBuilder(String fileName, String command, List<String> arguments, String evCommandPrefix){

        // reference file name
        this._fileName = fileName;

        // reference arguments
        List<String> args = arguments;

        // build process command
        String cmd = command;
        if (evCommandPrefix != null){

            String commandPrefix = getCommandPrefix(evCommandPrefix);
            for (int i = 0; i < args.size(); i++)
                args.set(i, args.get(i).replace("$" + evCommandPrefix, commandPrefix));

            cmd = String.format("%s/%s", commandPrefix, command);
        }

        // prepend the command name to the head of the list
        args.add(0, cmd);

        this._builder.command(args);
    }

    public Process start() throws ProcessBuilderException {

        try {
            return this._builder.start();
        }
        catch(IOException ex){
            throw new ProcessBuilderException(ex.toString());
        }
    }

    @Override
    public String toString(){
        return this._builder.command().toString();
    }

    public String getFileName(){
        return this._fileName;
    }

    /*
     * Get specified command prefix
     */
    private String getCommandPrefix(String ev){

        String prefix;
        Map<String, String> env = getEnvironmentalVariables();
        if (env.containsKey(ev)) {
            prefix = env.get(ev);
        }
        else {
            // todo: throw exception because specified variable does not exist?
            prefix = "";
        }

        return prefix;
    }

    /*
     * Retrieve the process builder's environmental variables map
     */
    private Map<String, String> getEnvironmentalVariables(){
        return _builder.environment();
    }
}
