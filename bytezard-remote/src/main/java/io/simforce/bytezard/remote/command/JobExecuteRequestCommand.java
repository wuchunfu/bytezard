package io.simforce.bytezard.remote.command;

public class JobExecuteRequestCommand extends BaseCommand {

    private String jobExecutionContext;

    public String getJobExecutionContext() {
        return jobExecutionContext;
    }

    public void setJobExecutionContext(String jobExecutionContext) {
        this.jobExecutionContext = jobExecutionContext;
    }

    public JobExecuteRequestCommand(){
        this.commandCode = CommandCode.JOB_EXECUTE_REQUEST;
    }

    public JobExecuteRequestCommand(String jobExecutionContext) {
        this.jobExecutionContext = jobExecutionContext;
        this.commandCode = CommandCode.JOB_EXECUTE_REQUEST;
    }
}
