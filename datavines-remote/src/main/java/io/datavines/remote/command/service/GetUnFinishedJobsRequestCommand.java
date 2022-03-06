package io.datavines.remote.command.service;

import io.datavines.remote.command.BaseCommand;
import io.datavines.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetUnFinishedJobsRequestCommand extends BaseCommand {

    public GetUnFinishedJobsRequestCommand(){
        this.commandCode = CommandCode.GET_UN_FINISHED_JOBS_REQUEST;
    }

}
