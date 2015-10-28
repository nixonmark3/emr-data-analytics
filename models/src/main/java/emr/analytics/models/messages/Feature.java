package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Feature<T> implements Serializable {

    private Class<T> clazz;
    private List<T> feature;

    public Feature(Class<T> clazz){
        this();

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
        this.feature.add(value);
    }

    public List<T> getFeature() { return this.feature; }

    private Feature(){
        feature = new ArrayList<T>();
    }
}