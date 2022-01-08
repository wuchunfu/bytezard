package io.simforce.bytezard.coordinator.repository.service;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.JobInstance;

/**
 * @author zixi0825
 */
public interface JobInstanceService  {

    /**
     * 返回主键字段id值
     * @param jobInstance
     * @return
     */
    long save(JobInstance jobInstance);

    /**
     * updateById
     * @param jobInstance
     * @return
     */
    int updateById(JobInstance jobInstance);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    JobInstance getById(long id);

    /**
     * 根据executionId获取job实例
     * @param executionId
     * @return
     */
    JobInstance getByExecutionId(long executionId);

    /**
     * 根据projectId获取jobInstance列表
     * @param projectId
     * @return
     */
    List<JobInstance> listByProjectId(long projectId);

}
