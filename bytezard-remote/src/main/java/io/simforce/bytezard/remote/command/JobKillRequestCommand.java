package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class JobKillRequestCommand extends BaseCommand{

    private Long taskId;

    public JobKillRequestCommand(){
        this.commandCode = CommandCode.JOB_KILL_REQUEST;
    }

    public JobKillRequestCommand(Long taskId){
        this.taskId = taskId;
        this.commandCode = CommandCode.JOB_KILL_REQUEST;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
