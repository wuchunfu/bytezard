package io.simforce.bytezard.executor.cache;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.executor.runner.JobRunner;

public class JobExecutionContext {

    private ExecutionJob executionJob;

    private JobRunner jobRunner;

    public ExecutionJob getExecutionJob() {
        return executionJob;
    }

    public void setExecutionJob(ExecutionJob executionJob) {
        this.executionJob = executionJob;
    }

    public JobRunner getJobRunner() {
        return jobRunner;
    }

    public void setJobRunner(JobRunner jobRunner) {
        this.jobRunner = jobRunner;
    }
}
