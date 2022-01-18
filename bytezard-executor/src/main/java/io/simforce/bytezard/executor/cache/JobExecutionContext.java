package io.simforce.bytezard.executor.cache;

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.executor.runner.TaskRunner;

public class JobExecutionContext {

    private TaskRequest taskRequest;

    private TaskRunner taskRunner;

    public TaskRequest getTaskRequest() {
        return taskRequest;
    }

    public void setTaskRequest(TaskRequest taskRequest) {
        this.taskRequest = taskRequest;
    }

    public TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }
}
