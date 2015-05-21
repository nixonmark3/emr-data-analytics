package emr.analytics.wrapper;

import org.jongo.MongoCollection;
import java.util.Set;
import org.reflections.Reflections;

public class DefinitionGenerator {

    MongoCollection _definitions = null;

    public DefinitionGenerator(MongoCollection definitionsCollection) {

        this._definitions = definitionsCollection;
    }

    public void generate() {

        Reflections reflections = new Reflections("emr.analytics.wrapper.definitions");
        Set<Class<? extends IExport>> blockDefinitionClasses = reflections.getSubTypesOf(IExport.class);

        for (Class<? extends IExport> blockDefinitionClass : blockDefinitionClasses) {

            try {

                System.out.println(blockDefinitionClass.getSimpleName());
                blockDefinitionClass.newInstance().export(_definitions);
            }
            catch (IllegalAccessException e) {

                e.printStackTrace();
            }
            catch (InstantiationException e) {

                e.printStackTrace();
            }
        }
    }
}
