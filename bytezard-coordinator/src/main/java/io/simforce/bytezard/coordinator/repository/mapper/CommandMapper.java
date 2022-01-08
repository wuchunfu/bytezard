package io.simforce.bytezard.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import io.simforce.bytezard.coordinator.repository.entity.Command;

/**
 * @author zixi0825
 */
public interface CommandMapper {

    /**
     * 返回主键字段id值
     * @param command
     * @return
     */
    @ResultMap("commandResultMap")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into bytezard_command " +
                "(type," +
                "parameter," +
                "project_id," +
                "priority," +
                "schedule_time," +
                "submit_time," +
                "start_time," +
                "create_time," +
                "update_time) " +
            "values " +
                "(#{type}," +
                "#{parameter}," +
                "#{projectId}," +
                "#{priority}," +
                "#{scheduleTime}," +
                "#{submitTime}," +
                "#{startTime}," +
                "#{createTime}," +
                "#{updateTime}) ")
    long save(Command command);

    /**
     * updateById
     * @param command
     * @return
     */
    @ResultMap("commandResultMap")
    @Update({ "update bytezard_command set " +
            "type = #{type}," +
            "parameter = #{parameter}," +
            "project_id = #{projectId}," +
            "priority = #{priority}," +
            "schedule_time = #{scheduleTime, jdbcType=TIMESTAMP}," +
            "submit_time = #{submitTime, jdbcType=TIMESTAMP}," +
            "start_time = #{startTime, jdbcType=TIMESTAMP}, " +
            "create_time = #{createTime, jdbcType=TIMESTAMP}," +
            "update_time = #{updateTime, jdbcType=TIMESTAMP} " +
            "where id = #{id}" })
    int updateById(Command command);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    @Results(id = "commandResultMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "type", column = "type"),
            @Result(property = "parameter", column = "parameter"),
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "priority", column = "priority"),
            @Result(property = "scheduleTime", column = "schedule_time"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    @Select("SELECT * from bytezard_command WHERE id = #{id} ")
    Command getById(long id);

    /**
     * SELECT BY ID
     * @return
     */
    @ResultMap("commandResultMap")
    @Select("SELECT * from bytezard_command order by update_time limit 1 ")
    Command getOne();

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    @Delete("delete from bytezard_command WHERE id = #{id} ")
    int deleteById(long id);
}
