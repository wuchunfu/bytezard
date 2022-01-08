package io.simforce.bytezard.remote.command.service;

import io.simforce.bytezard.remote.command.BaseCommand;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class PersistJobResponseCommand extends BaseCommand{

    private long jobInstanceId;

    public PersistJobResponseCommand(){
        this.commandCode = CommandCode.PERSIST_JOB_RESPONSE;
    }

    public PersistJobResponseCommand(long jobInstanceId){
        this.jobInstanceId = jobInstanceId;
        this.commandCode = CommandCode.PERSIST_JOB_RESPONSE;
    }

    public long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

}
