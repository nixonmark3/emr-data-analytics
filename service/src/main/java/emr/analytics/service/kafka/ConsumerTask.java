package emr.analytics.service.kafka;

import emr.analytics.models.messages.Consumers;
import emr.analytics.models.messages.TaskVariable;

public class ConsumerTask {

    private TaskVariable taskVariable;
    private Consumers consumers;

    public ConsumerTask(TaskVariable jobVariable, Consumers consumers) {

        this.taskVariable = jobVariable;
        this.consumers = consumers;
    }

    public TaskVariable getTaskVariable() { return this.taskVariable; }

    public Consumers getConsumers() { return this.consumers; }
}
