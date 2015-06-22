package emr.analytics.wrapper;

import emr.analytics.models.definition.*;

import org.jongo.MongoCollection;

import java.util.List;

public abstract class BlockDefinition implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = createDefinition();

        ModeDefinition modeDefinition = createOfflineMode();
        if (modeDefinition != null)
            definition.setModel(Mode.OFFLINE, modeDefinition);

        modeDefinition = createOnlineMode();
        if (modeDefinition != null)
            definition.setModel(Mode.ONLINE, modeDefinition);

        definitions.save(definition);
    }

    public abstract Definition createDefinition();
    public abstract ModeDefinition createOnlineMode();
    public abstract ModeDefinition createOfflineMode();
}
