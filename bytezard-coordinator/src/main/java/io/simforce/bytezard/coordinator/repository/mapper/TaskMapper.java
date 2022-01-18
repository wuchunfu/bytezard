package io.simforce.bytezard.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.Task;

/**
 * @author zixi0825
 */
public interface TaskMapper {

    /**
     * 返回主键字段id值
     * @param job
     * @return
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into bytezard_task " +
                "(name," +
                "job_id," +
                "parameter," +
                "json," +
                "project_id," +
                "status," +
                "retry_times," +
                "retry_interval," +
                "timeout," +
                "timeout_strategy," +
                "tenant_code,"+
                "env_file,"+
                "resources,"+
                "execution_id," +
                "submit_time," +
                "start_time," +
                "end_time," +
                "create_time," +
                "update_time) \n" +
            "values " +
                "(#{name}," +
                "#{jobId}," +
                "#{parameter}," +
                "#{json}," +
                "#{projectId}," +
                "#{status}," +
                "#{retryTimes}," +
                "#{retryInterval}," +
                "#{timeout}," +
                "#{timeoutStrategy}," +
                "#{tenantCode}," +
                "#{envFile}," +
                "#{resources}," +
                "#{executionId}," +
                "#{submitTime}," +
                "#{startTime}," +
                "#{endTime}," +
                "#{createTime}," +
                "#{updateTime}) ")
    int save(Task job);

    /**
     * updateById
     * @param job
     * @return
     */

    @Update({ "update bytezard_task set " +
            "name = #{name}," +
            "job_id = #{jobId}," +
            "parameter = #{parameter}," +
            "json = #{json}, " +
            "project_id = #{projectId}," +
            "status = #{status}," +
            "retry_times = #{retryTimes}, " +
            "retry_interval = #{retryInterval}," +
            "timeout = #{timeout}," +
            "timeout_strategy = #{timeoutStrategy}, " +
            "tenant_code = #{tenantCode}," +
            "env_file = #{envFile}," +
            "resources = #{resources}," +
            "execution_id = #{executionId}," +
            "submit_time = #{submitTime, jdbcType=TIMESTAMP}," +
            "start_time = #{startTime, jdbcType=TIMESTAMP}, " +
            "end_time = #{endTime, jdbcType=TIMESTAMP}," +
            "create_time = #{createTime, jdbcType=TIMESTAMP}," +
            "update_time = #{updateTime, jdbcType=TIMESTAMP} " +
            "where id = #{id}" })
    int updateById(Task job);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    @Results(id = "resultMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "name", column = "name"),
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "parameter", column = "parameter"),
            @Result(property = "json", column = "json"),
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "retryTimes", column = "retry_times" ),
            @Result(property = "retryInterval", column = "retry_interval"),
            @Result(property = "timeout", column = "timeout"),
            @Result(property = "timeoutStrategy", column = "timeout_strategy"),
            @Result(property = "tenantCode", column = "tenant_code"),
            @Result(property = "envFile", column = "env_file"),
            @Result(property = "resources", column = "resources"),
            @Result(property = "executionId", column = "execution_id"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    @Select("SELECT * from bytezard_task WHERE id = #{id} ")
    Task getById(long id);

    /**
     * SELECT BY ID
     * @param executionId
     * @return
     */
    @ResultMap("resultMap")
    @Select("SELECT * from bytezard_task WHERE execution_id = #{executionId} ")
    Task getByExecutionId(long executionId);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    @Delete("delete from bytezard_task WHERE id = #{id} ")
    int deleteById(long id);

    /**
     * LIST BY projectId
     * @param projectId
     * @return
     */
    @ResultMap("resultMap")
    @Select("SELECT * from bytezard_task WHERE project_id = #{projectId} ")
    List<Task> listByProjectId(long projectId);
}
