package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class JobKillAckCommand extends BaseCommand{

    private Long taskId;

    private int code;

    private String msg;

    public JobKillAckCommand(){
        this.commandCode = CommandCode.JOB_KILL_ACK;
    }

    public JobKillAckCommand(Long taskId){
        this.taskId = taskId;
        this.commandCode = CommandCode.JOB_KILL_ACK;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
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
