package io.datavines.remote.command.service;

import io.datavines.remote.command.BaseCommand;
import io.datavines.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetJobByIdRequestCommand extends BaseCommand {

    private long taskId;

    public GetJobByIdRequestCommand(){
        this.commandCode = CommandCode.GET_JOB_BY_ID_REQUEST;
    }

    public GetJobByIdRequestCommand(long taskId){
        this.taskId = taskId;
        this.commandCode = CommandCode.GET_JOB_BY_ID_REQUEST;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

}
