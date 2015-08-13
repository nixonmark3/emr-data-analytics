package emr.analytics.service.sources;

import java.util.List;

public interface StreamingSource { public SourceValues<Double> read(); }
