package io.simforce.bytezard.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.simforce.bytezard.coordinator.repository.entity.Job;
import io.simforce.bytezard.coordinator.repository.entity.Task;

@Mapper
public interface TaskMapper extends BaseMapper<Task>  {

    @Select("SELECT * from bytezard_task WHERE project_id = #{projectId} ")
    List<Task> listByProjectId(long projectId);
}
