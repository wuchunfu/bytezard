package io.simforce.bytezard.remote.command;

import java.util.Date;

public class JobExecuteResponseCommand extends BaseCommand {

    private long jobInstanceId;

    private int status;

    private Date endTime;

    private String applicationIds;

    private int processId;

    public JobExecuteResponseCommand(){
        this.commandCode = CommandCode.JOB_EXECUTE_RESPONSE;
    }

    public JobExecuteResponseCommand(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
        this.commandCode = CommandCode.JOB_EXECUTE_RESPONSE;
    }

    public long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getApplicationIds() {
        return applicationIds;
    }

    public void setApplicationIds(String applicationIds) {
        this.applicationIds = applicationIds;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

}
