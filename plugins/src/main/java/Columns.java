import emr.analytics.models.definition.Argument;
import emr.analytics.models.interfaces.DynamicSource;

import java.util.ArrayList;
import java.util.List;

public class Columns implements DynamicSource {
    public List<String> Execute(List<Argument> arguments) {
        List<String> stringList = new ArrayList<String>();
        stringList.add("Column1");
        stringList.add("Column2");
        stringList.add("Column3");
        stringList.add("Column4");
        stringList.add("Column5");
        return stringList;
    }
}
