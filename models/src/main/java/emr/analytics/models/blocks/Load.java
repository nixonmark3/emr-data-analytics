package emr.analytics.models.blocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Load implements Serializable {

    private List<DataFile> files = new ArrayList<DataFile>();

    private Load() {}

    public List<DataFile> getFiles() {

        return files;
    }

    public void setFiles(List<DataFile> files) {

        this.files = files;
    }
}
