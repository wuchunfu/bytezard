/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.simforce.bytezard.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import io.simforce.bytezard.common.entity.TaskRequest;

/**
 * execution job mapper
 * @author zixi0825
 */
public interface ExecutionJobMapper {

    /**
     * 返回主键字段id值
     * @param job
     * @return
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into bytezard_execution_job " +
                "(task_id,job_name," +
                "job_json," +
                "execute_path," +
                "log_path," +
                "result_path," +
                "execute_host," +
                "job_unique_id," +
                "process_id," +
                "application_ids," +
                "job_parameters," +
                "tenant_code," +
                "env_file," +
                "submit_time," +
                "start_time," +
                "end_time," +
                "queue," +
                "status," +
                "executor_group," +
                "timeout," +
                "timeout_strategy," +
                "retry_nums," +
                "resources," +
                "create_time," +
                "modify_time) " +
            "values " +
                "(#{taskId},#{jobName}," +
                "#{jobJson}," +
                "#{executePath}," +
                "#{logPath}," +
                "#{resultPath}," +
                "#{executeHost}," +
                "#{jobUniqueId}," +
                "#{processId}," +
                "#{applicationIds}," +
                "#{jobParameters}," +
                "#{tenantCode}," +
                "#{envFile}," +
                "#{submitTime}," +
                "#{startTime}," +
                "#{endTime}," +
                "#{queue}," +
                "#{status}," +
                "#{executorGroup}," +
                "#{timeout}," +
                "#{timeoutStrategy}," +
                "#{retryNums}," +
                "#{resources}," +
                "#{createTime}," +
                "#{modifyTime}) ")
    int save(TaskRequest job);

    /**
     * 获取尚未完成的作业
     * @return
     */
    @Results(id = "jobResultMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "jobName", column = "job_name"),
            @Result(property = "jobJson", column = "job_json"),
            @Result(property = "executePath", column = "execute_path"),
            @Result(property = "logPath", column = "log_path"),
            @Result(property = "resultPath", column = "result_path"),
            @Result(property = "executeHost", column = "execute_host" ),
            @Result(property = "jobUniqueId", column = "job_unique_id"),
            @Result(property = "processId", column = "process_id"),
            @Result(property = "applicationId", column = "application_id"),
            @Result(property = "jobParameters", column = "job_parameters"),
            @Result(property = "tenantCode", column = "tenant_code"),
            @Result(property = "envFile", column = "env_file"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "queue", column = "queue"),
            @Result(property = "status", column = "status"),
            @Result(property = "executorGroup", column = "executor_group"),
            @Result(property = "timeout", column = "timeout"),
            @Result(property = "timeoutStrategy", column = "timeout_strategy"),
            @Result(property = "retryNums", column = "retry_nums"),
            @Result(property = "resources", column = "resources"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "modifyTime", column = "modify_time")
    })
    @Select("SELECT * from bytezard_execution_job WHERE status in (1,2,10,11)")
    List<TaskRequest> getUnfinishedJobs();

    /**
     * 获取尚未开始的任务
     * @return
     */
    @ResultMap("jobResultMap")
    @Select("SELECT * from bytezard_execution_job WHERE status = 0 ")
    List<TaskRequest> getUnStartedJobs();

    /**
     * updateById
     * @param job
     * @return
     */
    @Update({ "update bytezard_execution_job set " +
                "job_name = #{jobName}," +
                "job_json = #{jobJson}," +
                "execute_path = #{executePath}," +
                "log_path = #{logPath}," +
                "result_path = #{resultPath}," +
                "execute_host = #{executeHost}, " +
                "job_unique_id = #{jobUniqueId}," +
                "process_id = #{processId}," +
                "application_id = #{applicationId}," +
                "job_parameters = #{jobParameters}, " +
                "tenant_code = #{tenantCode}," +
                "env_file = #{envFile}," +
                "submit_time = #{submitTime, jdbcType=TIMESTAMP}," +
                "start_time = #{startTime, jdbcType=TIMESTAMP}, " +
                "end_time = #{endTime, jdbcType=TIMESTAMP}," +
                "queue = #{queue}," +
                "status = #{status}," +
                "executor_group = #{executorGroup}, " +
                "timeout = #{timeout}," +
                "timeout_strategy = #{timeoutStrategy}, " +
                "retry_nums = #{retryNums}," +
                "resources = #{resources}," +
                "create_time = #{createTime, jdbcType=TIMESTAMP}," +
                "modify_time = #{modifyTime, jdbcType=TIMESTAMP} " +
            "where task_id = #{taskId}" })
    int updateById(TaskRequest job);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    @ResultMap("jobResultMap")
    @Select("SELECT * from bytezard_execution_job WHERE taskId = #{id} ")
    TaskRequest selectById(long id);
}
