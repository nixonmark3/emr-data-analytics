package emr.analytics.service.sources;

public class SourceValue<T> {

    private String key;
    private T value;

    public SourceValue(String key, T value){
        this.key = key;
        this.value = value;
    }

    public String getKey(){
        return this.key;
    }

    public T getValue(){
        return this.value;
    }

    @Override
    public String toString(){
        return String.format("(%s:%s)", this.getKey(), this.getValue().toString());
    }
}
