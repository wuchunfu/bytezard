package io.datavines.remote.command;

/**
 * @author zixi0825
 */
public class JobSubmitRequestCommand extends BaseCommand {

    private String jobExecutionContext;

    public String getJobExecutionContext() {
        return jobExecutionContext;
    }

    public void setJobExecutionContext(String jobExecutionContext) {
        this.jobExecutionContext = jobExecutionContext;
    }

    public JobSubmitRequestCommand(){
        this.commandCode = CommandCode.JOB_SUBMIT_REQUEST;
    }

    public JobSubmitRequestCommand(String jobExecutionContext) {
        this.jobExecutionContext = jobExecutionContext;
        this.commandCode = CommandCode.JOB_SUBMIT_REQUEST;
    }
}
