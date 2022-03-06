package io.datavines.remote.command;

import java.time.LocalDateTime;
import java.util.Date;

public class JobExecuteAckCommand extends BaseCommand{

    private long taskId;

    private LocalDateTime startTime;

    private String host;

    private int status;

    private String logPath;

    private String executePath;

    public JobExecuteAckCommand(long taskId) {
        this.taskId = taskId;
        this.commandCode = CommandCode.JOB_EXECUTE_ACK;
    }

    public JobExecuteAckCommand(){
        this.commandCode = CommandCode.JOB_EXECUTE_ACK;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getExecutePath() {
        return executePath;
    }

    public void setExecutePath(String executePath) {
        this.executePath = executePath;
    }
}
