package io.datavines.remote.command.service;

import io.datavines.remote.command.BaseCommand;
import io.datavines.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class UpdateJobResponseCommand extends BaseCommand {

    private long taskId;

    public UpdateJobResponseCommand(){
        this.commandCode = CommandCode.UPDATE_JOB_RESPONSE;
    }

    public UpdateJobResponseCommand(long taskId){
        this.taskId = taskId;
        this.commandCode = CommandCode.UPDATE_JOB_RESPONSE;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

}
