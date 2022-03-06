package io.datavines.remote.command.service;

import io.datavines.remote.command.BaseCommand;
import io.datavines.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class PersistJobResponseCommand extends BaseCommand {

    private long taskId;

    public PersistJobResponseCommand(){
        this.commandCode = CommandCode.PERSIST_JOB_RESPONSE;
    }

    public PersistJobResponseCommand(long taskId){
        this.taskId = taskId;
        this.commandCode = CommandCode.PERSIST_JOB_RESPONSE;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

}
