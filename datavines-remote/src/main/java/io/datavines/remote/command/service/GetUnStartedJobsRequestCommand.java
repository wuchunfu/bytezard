package io.datavines.remote.command.service;

import io.datavines.remote.command.BaseCommand;
import io.datavines.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetUnStartedJobsRequestCommand extends BaseCommand {

    public GetUnStartedJobsRequestCommand(){
        this.commandCode = CommandCode.GET_UN_STARTED_JOBS_REQUEST;
    }

}
