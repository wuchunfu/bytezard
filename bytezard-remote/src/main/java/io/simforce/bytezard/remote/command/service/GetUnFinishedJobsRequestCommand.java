package io.simforce.bytezard.remote.command.service;

import io.simforce.bytezard.remote.command.BaseCommand;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetUnFinishedJobsRequestCommand extends BaseCommand{

    public GetUnFinishedJobsRequestCommand(){
        this.commandCode = CommandCode.GET_UN_FINISHED_JOBS_REQUEST;
    }

}
