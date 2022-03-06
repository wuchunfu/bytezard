package io.datavines.coordinator.repository.service;

import java.util.List;

import io.datavines.coordinator.repository.entity.Job;

public interface JobService {

    /**
     * 返回主键字段id值
     * @param job
     * @return
     */
    long insert(Job job);

    /**
     * updateById
     * @param job
     * @return
     */
    int update(Job job);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    Job getById(long id);

    List<Job> listByProjectId(Long projectId);

    /**
     * delete by id
     * @param id id
     * @return int
     */
    int deleteById(long id);
}
