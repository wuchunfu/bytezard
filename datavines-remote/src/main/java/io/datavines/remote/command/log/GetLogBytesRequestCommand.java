package io.datavines.remote.command.log;

import io.datavines.remote.command.BaseCommand;
import io.datavines.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetLogBytesRequestCommand extends BaseCommand {

    private long taskId;

    private String path;

    public GetLogBytesRequestCommand(){
        this.commandCode = CommandCode.GET_LOG_BYTES_REQUEST;
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

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
}
