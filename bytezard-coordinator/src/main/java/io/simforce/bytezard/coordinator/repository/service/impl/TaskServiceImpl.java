package io.simforce.bytezard.coordinator.repository.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.simforce.bytezard.coordinator.repository.entity.Task;
import io.simforce.bytezard.coordinator.repository.mapper.TaskMapper;
import io.simforce.bytezard.coordinator.repository.service.TaskService;

@Service("taskService")
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>  implements TaskService {

    @Override
    public long insert(Task task) {
        baseMapper.insert(task);
        return task.getId();
    }

    @Override
    public int update(Task task) {
        return baseMapper.updateById(task);
    }

    @Override
    public Task getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<Task> listByProjectId(long projectId) {
        return baseMapper.listByProjectId(projectId);
    }
}
