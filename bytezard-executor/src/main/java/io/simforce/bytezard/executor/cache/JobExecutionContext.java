package io.simforce.bytezard.executor.cache;

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.executor.runner.JobRunner;

public class JobExecutionContext {

    private TaskRequest taskRequest;

    private JobRunner jobRunner;

    public TaskRequest getTaskRequest() {
        return taskRequest;
    }

    public void setTaskRequest(TaskRequest taskRequest) {
        this.taskRequest = taskRequest;
    }

    public JobRunner getJobRunner() {
        return jobRunner;
    }

    public void setJobRunner(JobRunner jobRunner) {
        this.jobRunner = jobRunner;
    }
}
