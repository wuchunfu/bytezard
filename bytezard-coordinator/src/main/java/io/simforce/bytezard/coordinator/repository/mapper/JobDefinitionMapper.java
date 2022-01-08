package io.simforce.bytezard.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.JobDefinition;

/**
 * @author zixi0825
 */
public interface JobDefinitionMapper {

    /**
     * 返回主键字段id值
     * @param jobDefinition
     * @return
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into bytezard_job_definition " +
            "(name," +
            "parameter," +
            "job_type," +
            "job_type_json," +
            "flow_definition_id," +
            "run_flag," +
            "pre_jobs," +
            "retry_times," +
            "retry_interval," +
            "timeout," +
            "timeout_strategy," +
            "tenant_code" +
            "env_file," +
            "resources," +
            "create_user_id," +
            "create_time," +
            "update_time) \n" +
            "values " +
            "(#{name}," +
            "#{parameter}," +
            "#{jobType}," +
            "#{jobTypeJson}," +
            "#{flow_definition_id}," +
            "#{runFlag}," +
            "#{preJobs}," +
            "#{retryTimes}," +
            "#{retryInterval}," +
            "#{timeout}," +
            "#{timeoutStrategy}," +
            "#{tenantCode}," +
            "#{envFile}," +
            "#{resources}," +
            "#{createUserId}," +
            "#{createTime}," +
            "#{updateTime}) ")
    int save(JobDefinition jobDefinition);

    /**
     * updateById
     * @param jobDefinition
     * @return
     */

    @Update({ "update bytezard_job_definition set " +
            "name = #{name}," +
            "parameter = #{parameter}," +
            "job_type = #{jobType}," +
            "job_type_json = #{jobTypeJson}, " +
            "flow_definition_id = #{flowDefinitionId}," +
            "run_flag = #{runFlag}," +
            "pre_jobs = #{preJobs}," +
            "retry_times = #{retryTimes}, " +
            "retry_interval = #{retryInterval}," +
            "timeout = #{timeout}," +
            "timeout_strategy = #{timeoutStrategy}, " +
            "tenant_code = #{tenantCode}," +
            "env_file = #{envFile}," +
            "resources = #{resources}," +
            "create_user_id = #{createUserId}," +
            "create_time = #{createTime, jdbcType=TIMESTAMP}," +
            "update_time = #{updateTime, jdbcType=TIMESTAMP} " +
            "where id = #{id}" })
    int updateById(JobDefinition jobDefinition);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    @Results(id = "resultMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "name", column = "name"),
            @Result(property = "parameter", column = "parameter"),
            @Result(property = "jobType", column = "job_type"),
            @Result(property = "jobTypeJson", column = "job_type_json"),
            @Result(property = "flowDefinitionId", column = "flow_definition_id"),
            @Result(property = "runFlag", column = "run_flag"),
            @Result(property = "preJobs", column = "pre_jobs"),
            @Result(property = "retryTimes", column = "retry_times" ),
            @Result(property = "retryInterval", column = "retry_interval"),
            @Result(property = "timeout", column = "timeout"),
            @Result(property = "timeoutStrategy", column = "timeout_strategy"),
            @Result(property = "tenantCode", column = "tenant_code"),
            @Result(property = "envFile", column = "env_file"),
            @Result(property = "resources", column = "resources"),
            @Result(property = "createUserId", column = "create_user_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    @Select("SELECT * from bytezard_job_definition WHERE id = #{id} ")
    JobDefinition getById(long id);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    @Delete("delete from bytezard_job_definition WHERE id = #{id} ")
    int deleteById(long id);

    /**
     * LIST BY flowDefinitionId
     * @param flowDefinitionId
     * @return
     */
    @ResultMap("resultMap")
    @Select("SELECT * from bytezard_job_definition WHERE flow_definition_id = #{flowDefinitionId} ")
    List<JobDefinition> listByFlowDefinitionId(long flowDefinitionId);

}
