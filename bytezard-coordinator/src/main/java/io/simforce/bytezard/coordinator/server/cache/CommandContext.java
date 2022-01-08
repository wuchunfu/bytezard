package io.simforce.bytezard.coordinator.server.cache;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class CommandContext {

    private CommandCode commandCode;

    private Long jobInstanceId;

    private ExecutionJob executionJob;

    public CommandContext(){}

    public CommandCode getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(CommandCode commandCode) {
        this.commandCode = commandCode;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public ExecutionJob getExecutionJob() {
        return executionJob;
    }

    public void setExecutionJob(ExecutionJob executionJob) {
        this.executionJob = executionJob;
    }
}
