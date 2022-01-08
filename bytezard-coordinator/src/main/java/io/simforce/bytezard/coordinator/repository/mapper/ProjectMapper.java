package io.simforce.bytezard.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.Project;

/**
 * @author zixi0825
 */
public interface ProjectMapper {

    /**
     * 返回主键字段id值
     * @param project
     * @return
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into bytezard_project " +
            "(name," +
            "description," +
            "create_user_id," +
            "version," +
            "create_time," +
            "update_time) \n" +
            "values " +
            "(#{name}," +
            "#{description}," +
            "#{createUserId}," +
            "#{version}," +
            "#{createTime}," +
            "#{updateTime}) ")
    int save(Project project);

    /**
     * updateById
     * @param projectFlow
     * @return
     */
    @Update({ "update bytezard_project set " +
            "name = #{name}," +
            "description = #{description}," +
            "version = #{version}," +
            "createUserId = #{create_user_id}," +
            "create_time = #{createTime, jdbcType=TIMESTAMP}," +
            "update_time = #{updateTime, jdbcType=TIMESTAMP} " +
            "where id = #{id}" })
    int updateById(Project projectFlow);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    @Results(id = "resultMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "name", column = "name"),
            @Result(property = "createUserId", column = "create_user_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "version", column = "version"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    @Select("SELECT * from bytezard_project WHERE id = #{id} ")
    Project getById(long id);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    @ResultMap("resultMap")
    @Delete("delete from bytezard_project WHERE id = #{id} ")
    int deleteById(long id);

    /**
     * list
     * @return
     */
    @ResultMap("resultMap")
    @Select("SELECT * from bytezard_project order by update_time")
    List<Project> list();
}
