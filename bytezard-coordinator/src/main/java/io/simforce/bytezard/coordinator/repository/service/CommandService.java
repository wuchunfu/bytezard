package io.simforce.bytezard.coordinator.repository.service;

import io.simforce.bytezard.coordinator.repository.entity.Command;

public interface CommandService  {

    /**
     * 返回主键字段id值
     * @param command
     * @return
     */
    long save(Command command);

    /**
     * updateById
     * @param command
     * @return
     */
    int updateById(Command command);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    Command getById(long id);

    /**
     * SELECT BY ID
     * @return
     */
    Command getOne();

    /**
     * delete by id
     * @param id id
     * @return int
     */
    int deleteById(long id);
}
