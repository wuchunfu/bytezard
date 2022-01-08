package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class JobSubmitAckCommand extends BaseCommand{

    private long jobInstanceId;

    private int code;

    private String msg;

    public JobSubmitAckCommand(){
        this.commandCode = CommandCode.JOB_SUBMIT_ACK;
    }

    public JobSubmitAckCommand(long jobInstanceId){
        this.jobInstanceId = jobInstanceId;
        this.commandCode = CommandCode.JOB_SUBMIT_ACK;
    }

    public long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(long jobInstanceId) {
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
