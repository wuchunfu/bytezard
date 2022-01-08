package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class JobSubmitKillRequestCommand extends BaseCommand{

    private Long jobInstanceId;

    public JobSubmitKillRequestCommand(){
        this.commandCode = CommandCode.JOB_SUBMIT_KILL_REQUEST;
    }

    public JobSubmitKillRequestCommand(Long jobInstanceId){
        this.jobInstanceId = jobInstanceId;
        this.commandCode = CommandCode.JOB_SUBMIT_KILL_REQUEST;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }
}
