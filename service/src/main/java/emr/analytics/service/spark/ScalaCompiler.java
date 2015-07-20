package emr.analytics.service.spark;

import emr.analytics.models.interfaces.RuntimeMessenger;
import scala.collection.JavaConversions;
import scala.collection.immutable.Nil;
import scala.reflect.internal.util.BatchSourceFile;
import scala.reflect.internal.util.SourceFile;
import scala.reflect.io.AbstractFile;
import scala.tools.nsc.Global;
import scala.tools.nsc.interpreter.AbstractFileClassLoader;
import scala.tools.nsc.Settings;
import scala.tools.nsc.settings.MutableSettings;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Class that compiles and executes scala code
 */
public class ScalaCompiler implements Callable {

    protected String _methodName = "start";
    protected final String _rootPath = "../output";
    protected String _className;
    protected String _code;
    protected File _dir;
    protected RuntimeMessenger _messenger;

    protected Global.Run _run;
    protected AbstractFileClassLoader _classLoader;
    protected HashMap<String, Class<?>> _classCache;

    /**
     * Class constructor - sets up the scala compiler and execution variables
     * @param code source code string to be executed
     * @param messenger used by the running code to pass message back to the originator
     * @throws ScalaCompilerException
     */
    public ScalaCompiler(String code, RuntimeMessenger messenger) throws ScalaCompilerException{

        _code = code;
        _messenger = messenger;
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

    /**
     * Callable interface method that allows you to asynchronous Java Futures
     * @return
     * @throws ScalaCompilerException
     */
    @Override
    public Boolean call() throws ScalaCompilerException {
        return this.run();
    }

    /**
     * Compile and run the specified scala code
     * @throws ScalaCompilerException
     */
    public Boolean run() throws ScalaCompilerException {
        // compile the specified code into a class
        Class<?> cls = compile();

        // invoke the compiled code passing a reference to this compiler to handle callbacks
        return this.invoke(cls, null);
    }

    /**
     * Invoke the specified source code
     * @param cls
     * @param args
     * @throws ScalaCompilerException
     */
    protected Boolean invoke(Class<?> cls, Object... args) throws ScalaCompilerException {

        // capture the classes of the input arguments
        // Class<?>[] classes = null;

        // append the configured runtimeMessenger object so that the executing code can pass pass messages
        // back to the originator
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(RuntimeMessenger.class);
        List<Object> arguments = new ArrayList<Object>();
        arguments.add(_messenger);

        if (args != null) {

            for(Object arg : args) {
                classes.add(arg.getClass());
                arguments.add(arg);
            }
        }

        // find the method
        Method method;
        try {
            method = cls.getMethod(_methodName, classes.toArray(new Class<?>[classes.size()]));
        }
        catch(NoSuchMethodException ex){
            String message = "Execution failed.  Unable to find 'start' method.  Actual exception: %s";
            throw new ScalaCompilerException(String.format(message, ex.toString()));
        }

        // invoke the method
        Boolean result;
        try {
            result = (Boolean)method.invoke(cls.newInstance(), arguments.toArray(new Object[arguments.size()]));
        }
        catch(InvocationTargetException ex){
            String message = "Execution failed.  Unable to invoke 'start' method.  Actual exception: %s";
            throw new ScalaCompilerException(String.format(message, ex.getCause().toString()));
        }
        catch(InstantiationException | IllegalAccessException ex){
            String message = "Execution failed.  Unable to invoke 'start' method.  Actual exception: %s";
            throw new ScalaCompilerException(String.format(message, ex.toString()));
        }

        return result;
    }

    /**
     * Find or (if necessary) compile the specified scala code into a class
     * @return the class representing the specified source code
     * @throws ScalaCompilerException
     */
    protected Class<?> compile() throws ScalaCompilerException {

        if (!findClass(_className).isPresent()) {

            // wrap the specified code in a class
            String classCode = wrapCodeInClass(_className, _code);

            // convert code to an iterable of objects (required for compilation)
            List<Object> classChars = new ArrayList<Object>();
            for(char c : classCode.toCharArray()) {
                classChars.add(c);
            }

            try {
                // compile
                SourceFile sf = new BatchSourceFile("(inline)", JavaConversions.iterableAsScalaIterable(classChars).toSeq());
                _run.compileSources(Nil.$colon$colon(sf));
            }
            catch(Exception ex){
                System.out.println(ex.toString());
            }
        }

        return findClass(_className).get();
    }

    /**
     * Generates a unique name for the class based on a hash of the specified code
     * @param code the source code to be executed
     * @return a repeatable unique name representing the specified source code
     * @throws ScalaCompilerException
     */
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


    /**
     * Looks for a class by name in the class hash map and the compiler's class loader
     * @param name class name
     * @return the specified class
     */
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

                System.out.println(ex.toString());

                return Optional.empty();
            }
        }
    }

    /**
     * Wraps the specified string of code into a class for execution
     * @param name the class name
     * @param code the code
     * @return string - code wrapped in a class
     */
    protected String wrapCodeInClass(String name, String code){
        return "import emr.analytics.models.interfaces.RuntimeMessenger \n"
                + "class " + name + "{\n"
                + "   def " + _methodName + "(messenger: RuntimeMessenger):Boolean = {\n"
                +  code + "\n"
                +  "true\n"
                + "   }\n"
                + "}\n";
    }

    /**
     * Create a jar file containing classes in the specified directory
     * @param jar jar file destination
     * @param dir directory of classes
     * @throws ScalaCompilerException
     */
    protected void createJar(File jar, File dir) throws ScalaCompilerException {

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        try {
            JarOutputStream target = new JarOutputStream(new FileOutputStream(jar), manifest);

            Arrays.stream(dir.listFiles()).filter(f -> f.getName().endsWith(".class")).forEach(f -> {

                try {

                    JarEntry entry = new JarEntry(f.getName());
                    entry.setTime(f.lastModified());
                    target.putNextEntry(entry);

                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
                    byte[] buffer = new byte[1024];
                    while (true)
                    {
                        int count = in.read(buffer);
                        if (count == -1)
                            break;
                        target.write(buffer, 0, count);
                    }
                    target.closeEntry();
                    in.close();
                }
                catch(IOException ex){
                    System.err.println("IO Exception: " + ex.toString());
                }});

            target.close();
        }
        catch(IOException ex){
            throw new ScalaCompilerException(ex.toString());
        }
    }
}
