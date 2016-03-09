package services;

import emr.analytics.models.definition.Argument;
import emr.analytics.models.definition.ParameterDefinition;
import emr.analytics.models.definition.ParameterSource;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.Parameter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SourcesService {

    private static String pluginPath = ConfigurationService.getPluginPath();

    public SourcesService(){}

    public void load(String name, ParameterDefinition parameter, Diagram diagram) {

        ParameterSource source = parameter.getSource();
        List<Argument> arguments = source.getArguments();

        // todo: temporarily this only handles block parameter references
        // todo: expand argument types to include support for block, connnector and diagram properties

        parameter.setFieldOptions(ExecuteJar(source.getFileName(),
                source.getClassName(),
                arguments));
    }

    public List<String> ExecuteJar(String file, String className, List<Argument> arguments){

        List<String> results = new ArrayList<String>();
        try {
            String jarPath = this.pluginPath + file;

            URL[] classpathURLs = new URL[]{
                    new File(jarPath).toURI().toURL()
            };

            ClassLoader loader = URLClassLoader.newInstance(classpathURLs,
                    getClass().getClassLoader());

            Class resourceClass = loader.loadClass(className);
            Method execute = resourceClass.getMethod("Execute", List.class);

            // todo: verify interface

            results = (List<String>)execute.invoke(resourceClass.newInstance(), arguments);
        }
        catch(java.lang.reflect.InvocationTargetException ex){
            System.err.println("Invocation Target Exception.");
            System.err.println(ex.getTargetException().toString());
        }
        catch(Exception ex){
            System.err.println(ex.toString());
        }

        return results;
    }
}
