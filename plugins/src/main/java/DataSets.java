
import emr.analytics.models.definition.Argument;
import emr.analytics.models.interfaces.DynamicSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DataSets implements DynamicSource {

    public List<String> Execute(List<Argument> arguments){

        // todo: perform some validation on the arguments received

        // reference the project name argument
        String projectName = arguments.get(0).getValue();

        return getDataSets().get(projectName);
    }

    public HashMap<String, List<String>> getDataSets(){

        HashMap<String, List<String>> sets = new HashMap<String, List<String>>();
        sets.put("Project 1",
                Arrays.<String>asList("DataSet 1 1", "DataSet 1 2", "DataSet 1 3", "DataSet 1 4"));
        sets.put("Project 2",
                Arrays.<String>asList("DataSet 2 1", "DataSet 2 2", "DataSet 2 3", "DataSet 2 4"));
        sets.put("Project 3",
                Arrays.<String>asList("DataSet 3 1", "DataSet 3 2", "DataSet 3 3", "DataSet 3 4"));
        sets.put("Project 4",
                Arrays.<String>asList("DataSet 4 1", "DataSet 4 2", "DataSet 4 3", "DataSet 4 4"));

        return sets;
    }
}
