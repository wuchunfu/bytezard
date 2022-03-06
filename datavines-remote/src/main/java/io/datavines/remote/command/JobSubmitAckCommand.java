package io.datavines.remote.command;

/**
 * @author zixi0825
 */
public class JobSubmitAckCommand extends BaseCommand{

    private long taskId;

    private int code;

    private String msg;

    public JobSubmitAckCommand(){
        this.commandCode = CommandCode.JOB_SUBMIT_ACK;
    }

    public JobSubmitAckCommand(long taskId){
        this.taskId = taskId;
        this.commandCode = CommandCode.JOB_SUBMIT_ACK;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
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
