package emr.analytics.service.spark;

import java.io.File;

import org.apache.spark.SparkContext;

/**
 * Class that compiles and executes spark code
 */
public class SparkCompiler extends ScalaCompiler {

    private SparkContext sc;

    public SparkCompiler(SparkContext sc, String code, RuntimeMessenger messenger) throws ScalaCompilerException {
        super(code, messenger);

        this.sc = sc;
    }

    /**
     * Compile, create jar, and run the spark code using the specified Spark Context
     * @throws ScalaCompilerException
     */
    @Override
    public Boolean run() throws ScalaCompilerException {

        // compile the specified code into a class
        Class<?> cls = compile();

        // reference / create class jar
        File jar = new File(String.format("%s.jar", _dir.getAbsolutePath()));
        if (!jar.exists())
            this.createJar(jar, _dir);

        // add jar to spark context so that
        sc.addJar(jar.getAbsolutePath());

        return this.invoke(cls, sc);
    }

    /**
     * Wraps the specified string of code into a class for execution
     * @param name the class name
     * @param code the code
     * @return string - code wrapped in a class
     */
    @Override
    protected String wrapCodeInClass(String name, String code){

        // todo: make argument list dynamic

        return "import org.apache.spark.SparkContext \n"
                + "import emr.analytics.service.spark.RuntimeMessenger \n"
                + "class " + name + "{\n"
                + "   def " + this._methodName + "(messenger: RuntimeMessenger, sc: SparkContext):Boolean = {\n"
                +  code + "\n"
                +  "true\n"
                + "   }\n"
                + "}\n";
    }
}
