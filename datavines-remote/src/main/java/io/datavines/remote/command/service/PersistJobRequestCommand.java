package io.datavines.remote.command.service;

import io.datavines.remote.command.BaseCommand;
import io.datavines.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class PersistJobRequestCommand extends BaseCommand {

    private String jobInfo;

    public String getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(String jobInfo) {
        this.jobInfo = jobInfo;
    }

    public PersistJobRequestCommand(){
        this.commandCode = CommandCode.PERSIST_JOB_REQUEST;
    }

    public PersistJobRequestCommand(String jobInfo) {
        this.jobInfo = jobInfo;
        this.commandCode = CommandCode.PERSIST_JOB_REQUEST;
    }
}
