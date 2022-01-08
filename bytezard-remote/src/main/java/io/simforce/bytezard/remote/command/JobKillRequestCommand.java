package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class JobKillRequestCommand extends BaseCommand{

    private Long jobInstanceId;

    public JobKillRequestCommand(){
        this.commandCode = CommandCode.JOB_KILL_REQUEST;
    }

    public JobKillRequestCommand(Long jobInstanceId){
        this.jobInstanceId = jobInstanceId;
        this.commandCode = CommandCode.JOB_KILL_REQUEST;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }
}
