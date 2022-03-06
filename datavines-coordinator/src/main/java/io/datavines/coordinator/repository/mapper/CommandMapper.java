package io.datavines.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.datavines.coordinator.repository.entity.Command;

@Mapper
public interface CommandMapper extends BaseMapper<Command> {

    /**
     * SELECT BY ID
     * @return
     */
    @Select("SELECT * from datavines_command order by update_time limit 1 ")
    Command getOne();
}
