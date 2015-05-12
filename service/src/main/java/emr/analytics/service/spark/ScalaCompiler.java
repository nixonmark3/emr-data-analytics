package emr.analytics.service.spark;

import scala.collection.JavaConversions;
import scala.collection.immutable.Nil;
import scala.reflect.internal.util.BatchSourceFile;
import scala.reflect.internal.util.SourceFile;
import scala.reflect.io.AbstractFile;
import scala.tools.nsc.Global;
import scala.tools.nsc.interpreter.AbstractFileClassLoader;
import scala.tools.nsc.Settings;
import scala.tools.nsc.settings.MutableSettings;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ScalaCompiler {

    protected String _methodName = "start";
    protected final String _rootPath = "../output";
    protected String _className;
    protected String _code;
    protected File _dir;

    protected Global.Run _run;
    protected AbstractFileClassLoader _classLoader;
    protected HashMap<String, Class<?>> _classCache;

    public ScalaCompiler(String code) throws ScalaCompilerException{

        _code = code;
        _className = generateClassName(_code);

        _dir = new File(String.format("%s/%s", _rootPath, _className));
        if (!_dir.exists())
            _dir.mkdirs();

        AbstractFile target = AbstractFile.getDirectory(scala.reflect.io.File.jfile2path(_dir));

        Settings settings = new Settings();
        ((MutableSettings.BooleanSetting)settings.deprecation()).v_$eq(true);
        ((MutableSettings.BooleanSetting)settings.unchecked()).v_$eq(true);
        settings.outputDirs().setSingleOutput(target);
        ((MutableSettings.BooleanSetting)settings.usejavacp()).v_$eq(true);

        _classCache = new HashMap<>();

        Global compiler = new Global(settings);
        _run = compiler.new Run();

        _classLoader = new AbstractFileClassLoader(target, this.getClass().getClassLoader());
    }

    /*
     * Compile and run the specified spark streaming driver code
    **/
    public void run() throws ScalaCompilerException {
        // compile the specified code into a class
        Class<?> cls = compile();

        this.invoke(cls, null);
    }

    protected void invoke(Class<?> cls, Object... args) throws ScalaCompilerException {

        // capture the classes of the input arguments
        Class<?>[] classes = null;
        if (args != null) {

            classes = Arrays.asList(args)
                    .stream()
                    .map(o -> o.getClass())
                    .toArray(Class<?>[]::new);
        }

        // find the method
        Method method;
        try {
            method = cls.getMethod(_methodName, classes);
        }
        catch(NoSuchMethodException ex){
            String message = "Execution failed.  Unable to find 'start' method.  Actual exception: %s";
            throw new ScalaCompilerException(String.format(message, ex.toString()));
        }

        // invoke the method
        try {
            method.invoke(cls.newInstance(), args);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException ex){
            String message = "Execution failed.  Unable to invoke 'start' method.  Actual exception: %s";
            throw new ScalaCompilerException(String.format(message, ex.toString()));
        }
    }

    /*
     * Find or (if necessary) compile the specified scala code
    **/
    protected Class<?> compile() throws ScalaCompilerException {

        if (!findClass(_className).isPresent()) {

            // wrap the specified code in a class
            String classCode = wrapCodeInClass(_className, _code);

            System.out.println("***Scala Code***");
            System.out.println(classCode);

            // convert code to an iterable of objects (required for compilation)
            List<Object> classChars = new ArrayList<Object>();
            for(char c : classCode.toCharArray()) {
                classChars.add(c);
            }

            // compile
            SourceFile sf = new BatchSourceFile("(inline)", JavaConversions.iterableAsScalaIterable(classChars).toSeq());
            _run.compileSources(Nil.$colon$colon(sf));
        }

        return findClass(_className).get();
    }

    /*
     * Generates a unique name for the class based on a hash of the specified code
    **/
    private String generateClassName(String code) throws ScalaCompilerException {
        try{
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(code.getBytes());
            return String.format("sha%s", (new BigInteger(1, digest)).toString(16));
        }
        catch (NoSuchAlgorithmException ex){
            String message = "Unable to generate a unique class name, the SHA-1 algorithm could not be found.  Actual exception: s%";
            throw new ScalaCompilerException(String.format(message, ex.toString()));
        }
    }


    /*
     * Looks for a class by name in the class hash map and the compiler's class loader
    **/
    private Optional<Class<?>> findClass(String name){
        if(_classCache.containsKey(name)){
            return Optional.of(_classCache.get(name));
        }
        else{
            try{
                Class<?> cls = _classLoader.loadClass(name);
                _classCache.put(name, cls);
                return Optional.of(cls);
            }
            catch(ClassNotFoundException ex){
                return Optional.empty();
            }
        }
    }

    /*
     * Wraps the specified code into a scala class for compilation
    **/
    protected String wrapCodeInClass(String name, String code){
        return "class " + name + "{\n"
                + "   def " + _methodName + "():Boolean = {\n"
                +  code + "\n"
                +  "true\n"
                + "   }\n"
                + "}\n";
    }
}
