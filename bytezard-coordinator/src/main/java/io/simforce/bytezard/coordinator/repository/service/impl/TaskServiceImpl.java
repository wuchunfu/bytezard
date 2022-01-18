package io.simforce.bytezard.coordinator.repository.service.impl;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.Task;
import io.simforce.bytezard.coordinator.repository.mapper.TaskMapper;
import io.simforce.bytezard.coordinator.repository.service.TaskService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TaskServiceImpl implements TaskService {

    @Inject
    private TaskMapper taskMapper;

    @Override
    public long save(Task task) {
        taskMapper.save(task);
        return task.getId();
    }

    @Override
    public int updateById(Task task) {
        return taskMapper.updateById(task);
    }

    @Override
    public Task getById(long id) {
        return taskMapper.getById(id);
    }

    @Override
    public Task getByExecutionId(long executionId) {
        return taskMapper.getByExecutionId(executionId);
    }

    @Override
    public List<Task> listByProjectId(long projectId) {
        return taskMapper.listByProjectId(projectId);
    }
}
