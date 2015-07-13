package controllers;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import play.libs.Json;
import play.mvc.Result;

public class Simulator extends Controller {

    public static Result read(){

        /*
        {"IN7OUT1MODEL/TAG1.CV":{"Type":"F","Val":"0.46"},
        "IN7OUT1MODEL/TAG2.CV":{"Type":"F","Val":"0.75"},
        "IN7OUT1MODEL/TAG3.CV":{"Type":"F","Val":"0.55"},
        "IN7OUT1MODEL/TAG4.CV":{"Type":"F","Val":"0.56"},
        "IN7OUT1MODEL/TAG5.CV":{"Type":"F","Val":"0.49"},
        "IN7OUT1MODEL/TAG6.CV":{"Type":"F","Val":"0.56"},
        "IN7OUT1MODEL/TAG7.CV":{"Type":"F","Val":"0.72"},
        "IN7OUT1MODEL/Y_PRED.CV":{"Type":"F","Val":"0.00"}
         */

        Map<String, WebDeltaV> values = new HashMap<String, WebDeltaV>();
        values.put("IN7OUT1MODEL/TAG1.CV", new WebDeltaV("F", Math.random()));
        values.put("IN7OUT1MODEL/TAG2.CV", new WebDeltaV("F", Math.random()));
        values.put("IN7OUT1MODEL/TAG3.CV", new WebDeltaV("F", Math.random()));
        values.put("IN7OUT1MODEL/TAG4.CV", new WebDeltaV("F", Math.random()));
        values.put("IN7OUT1MODEL/TAG5.CV", new WebDeltaV("F", Math.random()));
        values.put("IN7OUT1MODEL/TAG6.CV", new WebDeltaV("F", Math.random()));
        values.put("IN7OUT1MODEL/TAG7.CV", new WebDeltaV("F", Math.random()));

        return ok(Json.toJson(values));
    }

    @BodyParser.Of(BodyParser.Text.class)
    public static Result write(){

        System.out.println(request().body().asText());

        return ok("true");
    }

    public static class WebDeltaV implements Serializable {
        public String Type;
        public String Val;

        public WebDeltaV(String type, Double value){
            this.Type = type;
            this.Val = String.format("%.2f", value);
        }
    }
}
