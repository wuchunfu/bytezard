package io.simforce.bytezard.remote.command;

import java.time.LocalDateTime;
import java.util.Date;

public class JobExecuteResponseCommand extends BaseCommand {

    private long taskId;

    private int status;

    private LocalDateTime endTime;

    private String applicationIds;

    private int processId;

    public JobExecuteResponseCommand(){
        this.commandCode = CommandCode.JOB_EXECUTE_RESPONSE;
    }

    public JobExecuteResponseCommand(long taskId) {
        this.taskId = taskId;
        this.commandCode = CommandCode.JOB_EXECUTE_RESPONSE;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
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
