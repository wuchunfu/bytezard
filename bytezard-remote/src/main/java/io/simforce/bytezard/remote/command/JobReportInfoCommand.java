package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class JobReportInfoCommand extends BaseCommand {

    private long jobInstanceId;

    private String applicationIds;

    public JobReportInfoCommand(){
        this.commandCode = CommandCode.JOB_REPORT_INFO;
    }

    public JobReportInfoCommand(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
        this.commandCode = CommandCode.JOB_REPORT_INFO;
    }

    public JobReportInfoCommand(long jobInstanceId, String applicationIds) {
        this.jobInstanceId = jobInstanceId;
        this.applicationIds = applicationIds;
        this.commandCode = CommandCode.JOB_REPORT_INFO;
    }

    public long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public String getApplicationIds() {
        return applicationIds;
    }

    public void setApplicationIds(String applicationIds) {
        this.applicationIds = applicationIds;
    }


}
