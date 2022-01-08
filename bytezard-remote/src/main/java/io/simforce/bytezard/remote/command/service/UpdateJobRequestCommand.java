package io.simforce.bytezard.remote.command.service;

import io.simforce.bytezard.remote.command.BaseCommand;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class UpdateJobRequestCommand extends BaseCommand {

    private String jobInfo;

    public String getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(String jobInfo) {
        this.jobInfo = jobInfo;
    }

    public UpdateJobRequestCommand(){
        this.commandCode = CommandCode.UPDATE_JOB_REQUEST;
    }

    public UpdateJobRequestCommand(String jobInfo) {
        this.jobInfo = jobInfo;
        this.commandCode = CommandCode.UPDATE_JOB_REQUEST;
    }
}
