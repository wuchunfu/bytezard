package io.simforce.bytezard.common.entity;

/**
 * @author zixi0825
 */
public class ProcessResult {

    private Integer exitStatusCode;

    private String applicationId;

    private Integer processId;

    public ProcessResult(){
        this.exitStatusCode = -1;
        this.processId = -1;
    }

    public Integer getExitStatusCode() {
        return exitStatusCode;
    }

    public void setExitStatusCode(Integer exitStatusCode) {
        this.exitStatusCode = exitStatusCode;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }
}
