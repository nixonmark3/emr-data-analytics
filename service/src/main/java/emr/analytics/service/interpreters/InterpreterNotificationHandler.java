package emr.analytics.service.interpreters;

import emr.analytics.models.messages.Describe;

public interface InterpreterNotificationHandler {

    public void notify(InterpreterNotification notification);

    public void describe(Describe describe);
}
