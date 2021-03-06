package io.datavines.remote.command.service;

import io.datavines.remote.command.BaseCommand;
import io.datavines.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetUnFinishedJobsResponseCommand extends BaseCommand {

    private String jobs;

    public String getJobs() {
        return jobs;
    }

    public void setJobs(String jobs) {
        this.jobs = jobs;
    }

    public GetUnFinishedJobsResponseCommand(){
        this.commandCode = CommandCode.GET_UN_FINISHED_JOBS_RESPONSE;
    }

    public GetUnFinishedJobsResponseCommand(String jobs) {
        this.jobs = jobs;
        this.commandCode = CommandCode.GET_UN_FINISHED_JOBS_RESPONSE;
    }
}
