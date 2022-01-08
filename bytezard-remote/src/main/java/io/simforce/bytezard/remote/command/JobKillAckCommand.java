package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class JobKillAckCommand extends BaseCommand{

    private Long jobInstanceId;

    private int code;

    private String msg;

    public JobKillAckCommand(){
        this.commandCode = CommandCode.JOB_KILL_ACK;
    }

    public JobKillAckCommand(Long jobInstanceId){
        this.jobInstanceId = jobInstanceId;
        this.commandCode = CommandCode.JOB_KILL_ACK;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
