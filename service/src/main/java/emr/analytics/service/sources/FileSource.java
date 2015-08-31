package emr.analytics.service.sources;

import emr.analytics.models.messages.StreamingSourceRequest;

import java.io.*;
import java.util.*;

public class FileSource implements StreamingSource {

    private int index;
    private List<SourceValues<Double>> values;

    public FileSource(StreamingSourceRequest request){

        this.index = 0;
        this.readFile(request.getStreamingSource().getUrl(),
                request.getStreamingSource().getKeys());
    }

    public SourceValues<Double> read(){

        SourceValues<Double> sourceValues;
        if (this.index < this.values.size())
            sourceValues = this.values.get(this.index++);
        else
            sourceValues = null;
        return sourceValues;
    }

    private void readFile(String path, List<String> keys){

        this.values = new ArrayList<>();
        int[] indexes = new int[keys.size()];

        boolean initialized = false;
        try(BufferedReader reader = new BufferedReader(new FileReader(path))){

            String line;
            while ((line = reader.readLine()) != null) {

                // assumes CSV file
                String[] items = line.split(",");

                if (!initialized){
                    // assumes the file has a header row

                    // map column names and position
                    Map<String, Integer> columns = new HashMap<>();
                    int colIndex = 0;
                    for(String item : items)
                        columns.put(item, colIndex++);

                    // capture each key's index
                    colIndex = 0;
                    for(String key : keys) {
                        if (columns.containsKey(key))
                            indexes[colIndex++] = columns.get(key);
                        else
                            throw new SourceException(String.format("The column specified, '%s', does not exist.", key));
                    }

                    initialized = true;
                }
                else{
                    SourceValues<Double> sourceValues = new SourceValues<>();

                    int keyIndex = 0;
                    for(String key : keys){
                        sourceValues.add(key, Double.parseDouble(items[indexes[keyIndex]]));
                        keyIndex++;
                    }
                    this.values.add(sourceValues);
                }
            }
        }
        catch(IOException ex){
            throw new SourceException(ex);
        }
    }
}
