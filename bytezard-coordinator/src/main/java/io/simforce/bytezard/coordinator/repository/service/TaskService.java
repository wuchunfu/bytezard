package io.simforce.bytezard.coordinator.repository.service;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.Task;

public interface TaskService {

    /**
     * 返回主键字段id值
     * @param task
     * @return
     */
    long save(Task task);

    /**
     * updateById
     * @param task
     * @return
     */
    int updateById(Task task);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    Task getById(long id);

    /**
     * 根据executionId获取job实例
     * @param executionId
     * @return
     */
    Task getByExecutionId(long executionId);

    /**
     * 根据projectId获取task列表
     * @param projectId
     * @return
     */
    List<Task> listByProjectId(long projectId);

}
