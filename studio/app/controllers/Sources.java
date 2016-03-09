package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.definition.Argument;
import emr.analytics.models.definition.ParameterSource;
import emr.analytics.models.messages.TaskRequest;
import models.DynamicSourceRequest;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import services.*;

public class Sources extends Controller {

    /**
     * resolve dynamic parameter source
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result resolve() {

        DynamicSourceRequest request;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            request = objectMapper.convertValue(request().body().asJson(),
                    DynamicSourceRequest.class);

            ParameterSource parameterSource = request.getParameter().getSource();
            switch(parameterSource.getParameterSourceType()){
                case JAR:
                    // todo: add support for calls to jar plugin

                    return ok();

                case PYTHONSCRIPT:
                    // python scripts are evaluated by submitting a taskRequest to the analytics service
                    // the script should a response back to specified session via the interpreter

                    // format python script
                    Object[] arguments = parameterSource.getArguments().stream().map(Argument::getValue).toArray();
                    String script = String.format(parameterSource.getClassName(), arguments);

                    TaskRequest taskRequest = new TaskRequest(request.getSessionId(),
                            request.getDiagramId(),
                            request.getMode(),
                            request.getTargetEnvironment(),
                            request.getDiagramName(),
                            script);

                    // send request to analytics service
                    AnalyticsService.getInstance().send(taskRequest);

                    // return the task id for reference
                    return ok(taskRequest.getId().toString());

                default:

                    return internalServerError(String.format("The specified dynamic parameter source type, %s, is not supported.",
                            parameterSource.getParameterSourceType().toString()));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError("Failed to resolve sources.");
        }
    }
}
