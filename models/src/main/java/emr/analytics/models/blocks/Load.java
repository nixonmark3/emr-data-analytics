package emr.analytics.models.blocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Load implements Serializable {

    private List<String> files = new ArrayList<String>();

    private Load() {}

    public List<String> getFiles() {

        return files;
    }

    public void setFiles(List<String> files) {

        this.files = files;
    }
}
