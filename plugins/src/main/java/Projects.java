
import emr.analytics.models.definition.Argument;
import emr.analytics.models.interfaces.DynamicSource;

import java.util.ArrayList;
import java.util.List;

public class Projects implements DynamicSource {

    public List<String> Execute(List<Argument> arguments){

        List<String> results = new ArrayList<String>();
        results.add("Project 1");
        results.add("Project 2");
        results.add("Project 3");
        results.add("Project 4");

        return results;
    }
}
