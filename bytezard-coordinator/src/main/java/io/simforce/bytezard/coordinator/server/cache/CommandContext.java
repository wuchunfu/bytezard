package io.simforce.bytezard.coordinator.server.cache;

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.remote.command.CommandCode;

public class CommandContext {

    private CommandCode commandCode;

    private Long taskId;

    private TaskRequest taskRequest;

    public CommandContext(){}

    public CommandCode getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(CommandCode commandCode) {
        this.commandCode = commandCode;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public TaskRequest getTaskRequest() {
        return taskRequest;
    }

    public void setTaskRequest(TaskRequest taskRequest) {
        this.taskRequest = taskRequest;
    }
}
