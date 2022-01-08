package io.simforce.bytezard.remote.command.log;

import io.simforce.bytezard.remote.command.BaseCommand;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetLogBytesRequestCommand extends BaseCommand {

    private long jobInstanceId;

    private String path;

    public GetLogBytesRequestCommand(){
        this.commandCode =CommandCode.GET_LOG_BYTES_REQUEST;
    }

    public GetLogBytesRequestCommand(String path){
        this.path = path;
        this.commandCode =CommandCode.GET_LOG_BYTES_REQUEST;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }
}