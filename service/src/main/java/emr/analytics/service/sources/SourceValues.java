package emr.analytics.service.sources;

import java.util.ArrayList;
import java.util.List;

public class SourceValues<T> {

    private List<SourceValue<T>> values;

    public SourceValues(){
        this(new ArrayList<SourceValue<T>>());
    }

    public SourceValues(List<SourceValue<T>> values){
        this.values = values;
    }

    public void add(String key, T value){
        values.add(new SourceValue<T>(key, value));
    }

    public List<SourceValue<T>> getValues(){
        return this.values;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        values.forEach(x -> {
            builder.append(x.toString());
            builder.append(",");
        });
        return builder.substring(0, builder.length() - 1);
    }
}
