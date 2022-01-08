package io.simforce.bytezard.remote.command.service;

import io.simforce.bytezard.remote.command.BaseCommand;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetJobByIdResponseCommand extends BaseCommand {

    private String jobInfo;

    public String getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(String jobInfo) {
        this.jobInfo = jobInfo;
    }

    public GetJobByIdResponseCommand(){
        this.commandCode = CommandCode.GET_JOB_BY_ID_RESPONSE;
    }

    public GetJobByIdResponseCommand(String jobInfo) {
        this.jobInfo = jobInfo;
        this.commandCode = CommandCode.GET_JOB_BY_ID_RESPONSE;
    }
}
