package io.simforce.bytezard.coordinator.repository.service.impl;

import io.simforce.bytezard.coordinator.repository.entity.Command;
import io.simforce.bytezard.coordinator.repository.mapper.CommandMapper;
import io.simforce.bytezard.coordinator.repository.service.CommandService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author zixi0825
 */
@Singleton
public class CommandServiceImpl implements CommandService {

    @Inject
    private CommandMapper commandMapper;

    @Override
    public long save(Command command) {
        commandMapper.save(command);
        return command.getId();
    }

    @Override
    public int updateById(Command command) {
        return commandMapper.updateById(command);
    }

    @Override
    public Command getById(long id) {
        return commandMapper.getById(id);
    }

    @Override
    public Command getOne() {
        return commandMapper.getOne();
    }

    @Override
    public int deleteById(long id) {
        return commandMapper.deleteById(id);
    }
}
