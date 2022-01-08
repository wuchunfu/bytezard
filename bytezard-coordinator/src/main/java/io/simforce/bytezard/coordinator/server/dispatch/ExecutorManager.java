package io.simforce.bytezard.coordinator.server.dispatch;

import java.util.Collection;

import io.simforce.bytezard.coordinator.server.channel.ClientChannel;
import io.simforce.bytezard.coordinator.config.CoordinatorConfiguration;
import io.simforce.bytezard.coordinator.server.dispatch.strategy.ExecutorSelectStrategyFactory;
import io.simforce.bytezard.coordinator.server.dispatch.strategy.IExecutorSelectStrategy;
import io.simforce.bytezard.coordinator.server.dispatch.strategy.StrategyType;

/**
 * @author zixi0825
 */
public class ExecutorManager {

    private IExecutorSelectStrategy<ClientChannel> executorSelectStrategy;

    public ExecutorManager(CoordinatorConfiguration configuration){
        try{
            executorSelectStrategy = ExecutorSelectStrategyFactory
                    .getExecutorStrategy(StrategyType.of(
                            configuration.getString(
                                    CoordinatorConfiguration.COORDINATOR_HOST_SELECT_STRATEGY,
                                    CoordinatorConfiguration.COORDINATOR_HOST_SELECT_STRATEGY_DEFAULT)
                    ));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public ClientChannel select(Collection<ClientChannel> list) throws Exception{
        return executorSelectStrategy.select(list);
    }
}
