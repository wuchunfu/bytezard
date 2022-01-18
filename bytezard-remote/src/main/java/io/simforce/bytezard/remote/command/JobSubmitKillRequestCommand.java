package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class JobSubmitKillRequestCommand extends BaseCommand{

    private Long taskId;

    public JobSubmitKillRequestCommand(){
        this.commandCode = CommandCode.JOB_SUBMIT_KILL_REQUEST;
    }

    public JobSubmitKillRequestCommand(Long taskId){
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
