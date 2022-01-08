package io.simforce.bytezard.remote.command.service;

import io.simforce.bytezard.remote.command.BaseCommand;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class UpdateJobResponseCommand extends BaseCommand{

    private long jobInstanceId;

    public UpdateJobResponseCommand(){
        this.commandCode = CommandCode.UPDATE_JOB_RESPONSE;
    }

    public UpdateJobResponseCommand(long jobInstanceId){
        this.jobInstanceId = jobInstanceId;
        this.commandCode = CommandCode.UPDATE_JOB_RESPONSE;
    }

    public long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

}
