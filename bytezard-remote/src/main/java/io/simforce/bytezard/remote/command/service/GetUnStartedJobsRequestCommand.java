package io.simforce.bytezard.remote.command.service;

import io.simforce.bytezard.remote.command.BaseCommand;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetUnStartedJobsRequestCommand extends BaseCommand{

    public GetUnStartedJobsRequestCommand(){
        this.commandCode = CommandCode.GET_UN_STARTED_JOBS_REQUEST;
    }

}
