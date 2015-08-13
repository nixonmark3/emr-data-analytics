package emr.analytics.service.interpreters;

import java.io.Serializable;

public class InterpreterNotification implements Serializable {

    private String key;
    private String value;

    public InterpreterNotification(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String getKey(){
        return this.key;
    }

    public String getValue(){
        return this.value;
    }

    @Override
    public String toString() { return String.format("%s: %s", this.getKey(), this.getValue()); }
}
