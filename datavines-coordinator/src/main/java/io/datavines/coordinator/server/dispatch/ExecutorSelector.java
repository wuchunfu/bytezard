package io.datavines.coordinator.server.dispatch;

import java.util.Collection;

import io.datavines.coordinator.config.CoordinatorConfiguration;
import io.datavines.coordinator.server.dispatch.strategy.ExecutorSelectStrategyFactory;
import io.datavines.coordinator.server.dispatch.strategy.IExecutorSelectStrategy;
import io.datavines.coordinator.server.dispatch.strategy.StrategyType;
import io.datavines.coordinator.server.channel.ClientChannel;

/**
 * @author zixi0825
 */
public class ExecutorSelector {

    private IExecutorSelectStrategy<ClientChannel> executorSelectStrategy;

    public ExecutorSelector(CoordinatorConfiguration configuration){
        try{
            executorSelectStrategy = ExecutorSelectStrategyFactory
                    .getExecutorStrategy(StrategyType.of(configuration.getHostSelectStrategy()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public ClientChannel select(Collection<ClientChannel> list) throws Exception{
        return executorSelectStrategy.select(list);
    }
}
