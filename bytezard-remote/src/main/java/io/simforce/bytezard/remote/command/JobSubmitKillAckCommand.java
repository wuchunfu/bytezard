package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class JobSubmitKillAckCommand extends BaseCommand{

    private Long jobInstanceId;

    public JobSubmitKillAckCommand(){
        this.commandCode = CommandCode.JOB_SUBMIT_KILL_REQUEST;
    }

    public JobSubmitKillAckCommand(Long jobInstanceId){
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
