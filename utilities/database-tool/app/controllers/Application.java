package controllers;

import play.libs.*;
import play.mvc.*;

import plugins.JongoPlugin;

import views.html.*;

import java.net.UnknownHostException;

import org.jongo.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }

    public static Result getDatabaseNames() throws UnknownHostException {
        JongoPlugin plugin = JongoPlugin.getJongoPlugin();
        return ok(Json.toJson(plugin.getDatabaseNames()));
    }

    public static Result createDefinitions() {
        JongoPlugin plugin = JongoPlugin.getJongoPlugin();

        Jongo jongo = plugin.getJongoInstance("emr-data-analytics-studio");

        MongoCollection definitions = jongo.getCollection("definitions");

        // Remove all the definitions as we will recreate all of them
        definitions.drop();

        // Need to ensure that the name is a unique key so we don't get duplicates
        definitions.ensureIndex("{name: 1}","{unique:true}");

        DefinitionGenerator generator = new DefinitionGenerator();

        generator.generate(definitions);

        return ok();
    }

    public static Result createTestDiagram() {
        JongoPlugin plugin = JongoPlugin.getJongoPlugin();

        Jongo jongo = plugin.getJongoInstance("emr-data-analytics-studio");

        MongoCollection diagrams = jongo.getCollection("diagrams");

        // Remove all the diagrams as we will recreate all of them
        diagrams.drop();

        // Need to ensure that the name is a unique key so we don't get duplicates
        diagrams.ensureIndex("{name: 1}","{unique:true}");

        TestDiagramGenerator generator = new TestDiagramGenerator();

        diagrams.save(generator.generate());

        return ok();
    }
}
