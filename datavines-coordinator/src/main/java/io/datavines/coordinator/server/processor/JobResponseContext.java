package io.datavines.coordinator.server.processor;

import io.datavines.common.entity.TaskRequest;
import io.datavines.remote.command.CommandCode;

public class JobResponseContext {

    private CommandCode commandCode;

    private TaskRequest taskRequest;

    public JobResponseContext(CommandCode commandCode, TaskRequest taskRequest) {
        this.commandCode = commandCode;
        this.taskRequest = taskRequest;
    }

    public CommandCode getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(CommandCode commandCode) {
        this.commandCode = commandCode;
    }

    public TaskRequest getTaskRequest() {
        return taskRequest;
    }

    public void setTaskRequest(TaskRequest taskRequest) {
        this.taskRequest = taskRequest;
    }
}
