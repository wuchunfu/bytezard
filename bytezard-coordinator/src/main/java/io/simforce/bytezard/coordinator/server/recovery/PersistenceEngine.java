package io.simforce.bytezard.coordinator.server.recovery;

import java.util.List;

import io.simforce.bytezard.common.entity.TaskRequest;


/**
 * 持久化引擎的作用
 * - 将任务实时地更新到存储引擎中
 * - 定时将内存数据持久化到存储引擎中
 *
 * @author zixi0825
 */
public interface PersistenceEngine {

    /**
     * persist
     * @param name
     * @param job
     */
    long persist(String name, TaskRequest job);

    void unPersist(String name);

    void update(String name, TaskRequest job);

    List<TaskRequest> getUnStartedJobs();

    List<TaskRequest> getUnFinishedJobs();

    TaskRequest getById(long id);
 }
