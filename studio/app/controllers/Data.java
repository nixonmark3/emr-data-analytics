package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.CollectRequest;
import models.Query;
import models.UploadResult;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import play.Configuration;
import play.Play;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.FileService;
import services.WebClient;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Data extends Controller {

    private static final String ANALYTICS_CONFIG = "analytics";
    private static final String ANALYTICS_SHARE_NAME = "file.share";

    /**
     * Request an query object
     * @return
     */
    public static Result emptyQuery(){

        // InputStream stream = Data.class.getClassLoader().getResourceAsStream("hbase.json");
        // String query = WebClient.streamToString(stream);

        return ok(Json.toJson(new Query()));
    }

    /**
     *
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result hbaseQuery(){

        String filePath = getFileShare();
        String url = Configuration.root().getConfig("hbase").getString("url");

        ObjectMapper objectMapper = new ObjectMapper();
        Query query = objectMapper.convertValue(request().body().asJson(), Query.class);
        String queryString = Json.toJson(query).toString();

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("stream", queryString));

        String result = WebClient.post(url, parameters);
        result = result.replace("\t\n", "\n");

        String path = FileService.stringToFile(filePath, "hbase.tsv", result);

        return ok(path);
    }

    /**
     * Upload 1 to many files
     * @return: details about each uploaded file
     */
    public static Result upload(){

        String filePath = getFileShare();
        List<UploadResult> results = new ArrayList<UploadResult>();

        Http.MultipartFormData body = request().body().asMultipartFormData();
        for(Http.MultipartFormData.FilePart part : body.getFiles()){

            if (part != null){
                File uploaded = part.getFile();
                File file = new File(filePath, part.getFilename());

                UploadResult result = new UploadResult();
                result.setContentType(part.getContentType());
                result.setName(part.getFilename());
                result.setPath(file.getAbsolutePath());

                // move each file to the designated upload path (replacing existing files)
                try {
                    Files.move(uploaded.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch(java.io.IOException ex){
                    ex.printStackTrace();
                    // todo: notify user of exception
                }

                results.add(result);
            }
        }

        return ok(Json.toJson(results));
    }

    private static String getFileShare(){
        return Configuration.root().getConfig(ANALYTICS_CONFIG).getString(ANALYTICS_SHARE_NAME);
    }
}
