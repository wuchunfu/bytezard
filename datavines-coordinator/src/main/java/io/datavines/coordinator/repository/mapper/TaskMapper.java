package io.datavines.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.datavines.coordinator.repository.entity.Task;

@Mapper
public interface TaskMapper extends BaseMapper<Task>  {

    @Select("SELECT * from datavines_task WHERE project_id = #{projectId} ")
    List<Task> listByProjectId(long projectId);
}
