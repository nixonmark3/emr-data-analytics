package emr.analytics.models.diagram;

import java.io.Serializable;
import java.util.UUID;

public class ParameterOverride implements Serializable {

    private UUID _blockId = null;
    private String _name = "";
    private Object _value = null;

    private ParameterOverride() {}

    public ParameterOverride(UUID blockId, String name, Object value) {

        this._blockId = blockId;
        this._name = name;
        this._value = value;
    }

    public UUID get_blockId() {

        return _blockId;
    }

    public void set_blockId(UUID _blockId) {

        this._blockId = _blockId;
    }

    public String get_name() {

        return _name;
    }

    public void set_name(String _name) {

        this._name = _name;
    }

    public Object get_value() {

        return _value;
    }

    public void set_value(Object _value) {
        this._value = _value;
    }
}
