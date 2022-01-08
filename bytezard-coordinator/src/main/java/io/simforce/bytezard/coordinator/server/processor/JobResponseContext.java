package io.simforce.bytezard.coordinator.server.processor;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class JobResponseContext {

    private CommandCode commandCode;

    private ExecutionJob executionJob;

    public JobResponseContext(CommandCode commandCode, ExecutionJob executionJob) {
        this.commandCode = commandCode;
        this.executionJob = executionJob;
    }

    public CommandCode getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(CommandCode commandCode) {
        this.commandCode = commandCode;
    }

    public ExecutionJob getExecutionJob() {
        return executionJob;
    }

    public void setExecutionJob(ExecutionJob executionJob) {
        this.executionJob = executionJob;
    }
}
