package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.definition.ParameterDefinition;
import emr.analytics.models.definition.ParameterSourceRequest;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;

import services.*;

public class Sources extends ControllerBase {

    private static String pluginPath = ConfigurationService.getPluginPath();

    @BodyParser.Of(BodyParser.Json.class)
    public static Result load() {

        ParameterSourceRequest request;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            request = objectMapper.convertValue(request().body().asJson(),
                    ParameterSourceRequest.class);

            SourcesService service = new SourcesService(pluginPath);
            for(ParameterDefinition parameter : request.getParameters()){

                service.load(request.getName(),
                        parameter,
                        request.getDiagram());
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError(String.format("Failed to resolve sources."));
        }

        return ok(Json.toJson(request));
    }
}
