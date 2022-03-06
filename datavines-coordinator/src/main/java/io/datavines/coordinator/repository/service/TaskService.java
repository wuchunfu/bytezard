package io.datavines.coordinator.repository.service;

import java.util.List;

import io.datavines.coordinator.repository.entity.Task;

public interface TaskService {

    /**
     * 返回主键字段id值
     * @param task
     * @return
     */
    long insert(Task task);

    /**
     * updateById
     * @param task
     * @return
     */
    int update(Task task);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    Task getById(long id);

    /**
     * 根据projectId获取task列表
     * @param projectId
     * @return
     */
    List<Task> listByProjectId(long projectId);

}
