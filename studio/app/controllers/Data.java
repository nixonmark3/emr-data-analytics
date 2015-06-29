package controllers;

import models.project.UploadResult;
import play.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Data extends Controller {

    // reference the path for uploaded files
    private static String _rootPath = Play.application().path().getAbsolutePath() + "/../output";

    /*
    ** Upload 1 to many files
    ** Returns: details about each uploaded file
     */
    public static Result upload(){

        List<UploadResult> results = new ArrayList<UploadResult>();

        Http.MultipartFormData body = request().body().asMultipartFormData();
        for(Http.MultipartFormData.FilePart part : body.getFiles()){

            if (part != null){
                File uploaded = part.getFile();
                File file = new File(_rootPath, part.getFilename());

                UploadResult result = new UploadResult();
                result.setContentType(part.getContentType());
                result.setName(part.getFilename());
                result.setPath(file.getAbsolutePath());

                // move each file to the designated upload path (replacing existing files)
                try {
                    Files.move(uploaded.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch(java.io.IOException ex){
                    System.err.println(ex.getMessage());
                    // todo: notify user of exception
                }

                results.add(result);
            }
        }

        return ok(Json.toJson(results));
    }
}
