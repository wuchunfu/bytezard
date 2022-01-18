package io.simforce.bytezard.coordinator.repository.service;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.Job;

public interface JobService {

    /**
     * 返回主键字段id值
     * @param job
     * @return
     */
    long save(Job job);

    /**
     * updateById
     * @param job
     * @return
     */
    int updateById(Job job);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    Job getById(long id);

    /**
     * 根据 flow definition id 获取 flow job definition 列表
     * @param flowDefinitionId
     * @return
     */
    List<Job> listByFlowDefinitionId(Long flowDefinitionId);

    /**
     * delete by id
     * @param id id
     * @return int
     */
    int deleteById(long id);
}
