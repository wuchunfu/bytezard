package io.simforce.bytezard.coordinator.server.dispatch.strategy;

import io.simforce.bytezard.coordinator.server.channel.ClientChannel;

/**
 * @author zixi0825
 */
public class ExecutorSelectStrategyFactory {

    public static IExecutorSelectStrategy<ClientChannel> getExecutorStrategy(StrategyType strategyType) throws Exception{
        switch (strategyType){
            case RANDOM:
                return new RandomSelectStrategy();
            case ROUND_ROBIN:
                return new RoundRobinSelectStrategy();
            default:
                throw new Exception("invalid strategy");
        }
    }
}
