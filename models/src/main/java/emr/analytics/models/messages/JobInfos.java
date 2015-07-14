package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JobInfos extends BaseMessage implements Serializable {

    private List<JobInfo> items;

    public JobInfos(){
        super("job-infos");

        this.items = new ArrayList<JobInfo>();
    }

    public List<JobInfo> getItems() {
        return this.items;
    }

    public void add(JobInfo item){
        items.add(item);
    }
}
