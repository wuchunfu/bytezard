package io.simforce.bytezard.remote.command.service;

import io.simforce.bytezard.remote.command.BaseCommand;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetJobByIdRequestCommand extends BaseCommand{

    private long jobInstanceId;

    public GetJobByIdRequestCommand(){
        this.commandCode = CommandCode.GET_JOB_BY_ID_REQUEST;
    }

    public GetJobByIdRequestCommand(long jobInstanceId){
        this.jobInstanceId = jobInstanceId;
        this.commandCode = CommandCode.GET_JOB_BY_ID_REQUEST;
    }

    public long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

}
