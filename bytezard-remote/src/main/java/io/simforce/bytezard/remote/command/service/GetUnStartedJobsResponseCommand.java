package io.simforce.bytezard.remote.command.service;

import io.simforce.bytezard.remote.command.BaseCommand;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetUnStartedJobsResponseCommand extends BaseCommand {

    private String jobs;

    public String getJobs() {
        return jobs;
    }

    public void setJobs(String jobs) {
        this.jobs = jobs;
    }

    public GetUnStartedJobsResponseCommand(){
        this.commandCode = CommandCode.GET_JOB_BY_ID_RESPONSE;
    }

    public GetUnStartedJobsResponseCommand(String jobs) {
        this.jobs = jobs;
        this.commandCode = CommandCode.GET_JOB_BY_ID_RESPONSE;
    }
}
