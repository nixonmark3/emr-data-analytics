package emr.analytics.service.jobs;

import emr.analytics.models.definition.Mode;

import java.io.Serializable;
import java.util.UUID;

public class JobKey implements Serializable {

    private UUID id;
    private Mode mode;

    public JobKey(UUID id, Mode mode){
        this.id = id;
        this.mode = mode;
    }

    public UUID getId(){
        return this.id;
    }

    public Mode getMode(){
        return this.mode;
    }

    @Override
    public String toString(){
        return String.format("%s_%s", this.id.toString(), this.mode.toString());
    }
}
