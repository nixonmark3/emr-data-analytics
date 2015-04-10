package emr.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;

import emr.analytics.models.messages.BlockStatus;
import emr.analytics.models.messages.EvaluationStatus;

import java.io.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;

public class PythonTask {
    private UUID _id;
    private String _source;
    private String _path = "temp";
    private ServiceSocketCallback _socketCallback = null;

    public PythonTask(UUID id, String source) {
        _id = id;
        _source = source;
        _socketCallback = new ServiceSocketCallback();
    }

    public void execute() {
        this.compile();

        try {
            ProcessBuilder builder = new ProcessBuilder("python", getFileName());
            Process process = builder.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String lineRead;
            while ((lineRead = in.readLine()) != null) {
                System.out.println("status: " + lineRead);

                String[] data = lineRead.split(",");
                if (data.length == 2) {
                    sendEvaluationStatusForBlock(new BlockStatus(data[0], Integer.valueOf(data[1])));
                }
            }

            try {
                int complete = process.waitFor();

                if (complete != 0) {
                    System.err.println("Python script failed!");

                    while ((lineRead = err.readLine()) != null) {
                        System.err.println(lineRead);
                    }

                    // todo send back error message

                    sendEvaluationStatus(2); // failed
                }
                else {
                    System.out.println("Python Task Complete!");

                    sendEvaluationStatus(1); // success
                }
            }
            catch(InterruptedException ex) {
                System.err.println(String.format("InterruptedException: %s.", ex.toString()));
            }
            finally {
                in.close();
                err.close();
                process.destroy();
            }

            // TODO error handling for non-zero rc
        }
        catch(IOException ex) {
            System.err.println("IO Exception occurred.");
        }

        this.cleanup();
    }

    private String getFileName()
    {
        return String.format("%s/%s.py", _path, _id.toString());
    }

    private void compile() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(getFileName()));
            out.write(_source);
            out.close();
        }
        catch(IOException ex) {
            System.err.println("IO Exception occurred.");
        }
    }

    private void cleanup() {
        try {
            this._socketCallback.close();
            this._socketCallback = null;
            Files.delete(Paths.get(getFileName()));
        }
        catch(IOException ex) {
            System.err.println("IO Exception occurred.");
        }
    }

    private void sendEvaluationStatusForBlock(BlockStatus blockStatus) {
        EvaluationStatus evaluationStatus = new EvaluationStatus(this._id, 0); // in progress
        ArrayList<BlockStatus> list = new ArrayList<BlockStatus>();
        list.add(blockStatus);
        evaluationStatus.setBlockStatusList(list);
        _socketCallback.sendMessage(new ObjectMapper().valueToTree(evaluationStatus));
    }

    private void sendEvaluationStatus(int state) {
        _socketCallback.sendMessage(new ObjectMapper().valueToTree(new EvaluationStatus(this._id, state)));
    }
}
