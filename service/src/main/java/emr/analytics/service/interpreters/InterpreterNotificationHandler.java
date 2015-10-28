package emr.analytics.service.interpreters;

import emr.analytics.models.messages.Describe;
import emr.analytics.models.messages.Features;

public interface InterpreterNotificationHandler {

    public void collect(Features features);

    public void describe(Describe describe);

    public void notify(InterpreterNotification notification);
}
