package io.datavines.remote.command.log;

import io.datavines.remote.command.BaseCommand;
import io.datavines.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class RollViewLogRequestCommand extends BaseCommand {

    private long taskId;
    /**
     *  log path
     */
    private String path;

    /**
     *  skip line number
     */
    private int skipLineNum;

    /**
     *  query line number
     */
    private int limit;

    public RollViewLogRequestCommand() {
        this.commandCode = CommandCode.ROLL_VIEW_LOG_REQUEST;
    }

    public RollViewLogRequestCommand(String path, int skipLineNum, int limit) {
        this.commandCode = CommandCode.ROLL_VIEW_LOG_REQUEST;
        this.path = path;
        this.skipLineNum = skipLineNum;
        this.limit = limit;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSkipLineNum() {
        return skipLineNum;
    }

    public void setSkipLineNum(int skipLineNum) {
        this.skipLineNum = skipLineNum;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
}
