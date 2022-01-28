package io.simforce.bytezard.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.simforce.bytezard.coordinator.repository.entity.Command;

@Mapper
public interface CommandMapper extends BaseMapper<Command> {

    /**
     * SELECT BY ID
     * @return
     */
    @Select("SELECT * from bytezard_command order by update_time limit 1 ")
    Command getOne();
}
