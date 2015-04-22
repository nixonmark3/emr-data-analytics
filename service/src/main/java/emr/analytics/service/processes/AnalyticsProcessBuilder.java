package emr.analytics.service.processes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AnalyticsProcessBuilder {


    private ProcessBuilder _builder = new ProcessBuilder();

    public AnalyticsProcessBuilder(String command){
        this(command, new ArrayList<String>(), null);
    }

    public AnalyticsProcessBuilder(String command, List<String> arguments){
        this(command, arguments, null);
    }

    public AnalyticsProcessBuilder(String command, String evCommandPrefix){
        this(command, new ArrayList<String>(), evCommandPrefix);
    }

    public AnalyticsProcessBuilder(String command, List<String> arguments, String evCommandPrefix){

        // reference arguments
        List<String> args = arguments;

        // build process command
        String cmd = (evCommandPrefix != null)
            ? String.format("%s/%s", getCommandPrefix(evCommandPrefix), command)
            : command;

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
