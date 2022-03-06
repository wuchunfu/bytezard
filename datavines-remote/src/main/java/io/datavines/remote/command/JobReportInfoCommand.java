package io.datavines.remote.command;

/**
 * @author zixi0825
 */
public class JobReportInfoCommand extends BaseCommand {

    private long taskId;

    private String applicationIds;

    public JobReportInfoCommand(){
        this.commandCode = CommandCode.JOB_REPORT_INFO;
    }

    public JobReportInfoCommand(long taskId) {
        this.taskId = taskId;
        this.commandCode = CommandCode.JOB_REPORT_INFO;
    }

    public JobReportInfoCommand(long taskId, String applicationIds) {
        this.taskId = taskId;
        this.applicationIds = applicationIds;
        this.commandCode = CommandCode.JOB_REPORT_INFO;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getApplicationIds() {
        return applicationIds;
    }

    public void setApplicationIds(String applicationIds) {
        this.applicationIds = applicationIds;
    }


}
