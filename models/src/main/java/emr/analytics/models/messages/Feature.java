package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Feature<T> implements Serializable {

    private Class<T> clazz;
    private String name;
    private List<T> data;

    public Feature(String name, Class<T> clazz){
        this();

        this.name = name;
        this.clazz = clazz;
    }

    public void addObject(Object obj){

        T value;
        try{
            value = clazz.cast(obj);
        }
        catch(ClassCastException ex){
            throw new AnalyticsException(String.format("Invalid feature cast. Additional info: %s",
                    ex.getMessage()));
        }

        this.add(value);
    }

    public void add(T value){
        this.data.add(value);
    }

    public List<T> getData() { return this.data; }

    public String getName() { return this.name; }

    private Feature(){
        data = new ArrayList<T>();
    }
}