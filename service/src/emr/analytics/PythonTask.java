package emr.analytics;

import java.io.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.UUID;

public class PythonTask {

    private UUID _id;
    private String _source;
    private String _path = "temp";

    public PythonTask(UUID id, String source){

        _id = id;
        _source = source;
    }

    public PythonTask(String code) { this(UUID.randomUUID(), code); }

    public String execute(){

        String result = "";

        this.compile();

        try{
            ProcessBuilder builder = new ProcessBuilder("python", getFileName());
            Process process = builder.start();

            BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
            result = out.readLine();
        }
        catch(IOException ex){
            System.err.println("IO Exception occurred.");
        }

        this.cleanup();

        return result;
    }

    private String getFileName(){
        return String.format("%s/%s.py", _path, _id.toString());
    }

    private void compile(){

        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(getFileName()));
            out.write(_source);
            out.close();
        }
        catch(IOException ex){
            System.err.println("IO Exception occurred.");
        }
    }

    private void cleanup(){

        try{
            Files.delete(Paths.get(getFileName()));
        }
        catch(IOException ex){
            System.err.println("IO Exception occurred.");
        }

    }
}
