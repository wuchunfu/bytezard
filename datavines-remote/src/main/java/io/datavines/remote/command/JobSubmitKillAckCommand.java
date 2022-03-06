package io.datavines.remote.command;

/**
 * @author zixi0825
 */
public class JobSubmitKillAckCommand extends BaseCommand{

    private Long taskId;

    public JobSubmitKillAckCommand(){
        this.commandCode = CommandCode.JOB_SUBMIT_KILL_REQUEST;
    }

    public JobSubmitKillAckCommand(Long taskId){
        this.taskId = taskId;
        this.commandCode = CommandCode.JOB_SUBMIT_KILL_REQUEST;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
