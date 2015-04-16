package emr.analytics.models.interfaces;

import emr.analytics.models.definition.Argument;

import java.util.List;

public interface DynamicSource {

    public List<String> Execute(List<Argument> arguments);
}
