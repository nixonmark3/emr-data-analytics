package emr.analytics.service.spark;

import java.io.*;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.spark.SparkContext;
import org.apache.spark.streaming.StreamingContext;

public class SparkStreamingCompiler extends ScalaCompiler {

    public SparkStreamingCompiler(String code) throws ScalaCompilerException{ super(code); }

    public void run(StreamingContext ssc) throws ScalaCompilerException {

        // compile the specified code into a class
        Class<?> cls = compile();

        // reference / create class jar
        File jar = new File(String.format("%s.jar", _dir.getAbsolutePath()));
        if (!jar.exists())
            this.createJar(jar, _dir);

        // add jar to spark context so that
        SparkContext sc = ssc.sparkContext();
        sc.addJar(jar.getAbsolutePath());

        this.invoke(cls, ssc);
    }

    @Override
    protected String wrapCodeInClass(String name, String code){
        return "import org.apache.spark.streaming.StreamingContext \n"
            + "class " + name + "{\n"
            + "   def " + this._methodName + "(ssc: StreamingContext):Boolean = {\n"
            +  code + "\n"
            +  "true\n"
            + "   }\n"
            + "}\n";
    }

    /*
    ** Create a jar containing classes in the specified directory
     */
    private void createJar(File jar, File dir) throws ScalaCompilerException{

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
