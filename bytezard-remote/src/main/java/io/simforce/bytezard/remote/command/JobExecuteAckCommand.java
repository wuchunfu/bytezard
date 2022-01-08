package io.simforce.bytezard.remote.command;

import java.util.Date;

public class JobExecuteAckCommand extends BaseCommand{

    private long jobInstanceId;

    private Date startTime;

    private String host;

    private int status;

    private String logPath;

    private String executePath;

    public JobExecuteAckCommand(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
        this.commandCode = CommandCode.JOB_EXECUTE_ACK;
    }

    public JobExecuteAckCommand(){
        this.commandCode = CommandCode.JOB_EXECUTE_ACK;
    }

    public long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
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
