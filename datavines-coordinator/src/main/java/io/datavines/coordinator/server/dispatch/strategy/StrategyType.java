package io.datavines.coordinator.server.dispatch.strategy;

/**
 * @author zixi0825
 */
public enum StrategyType {
    /**
     * 1. 随机
     * 2. 轮询
     */
    RANDOM,
    ROUND_ROBIN;

    public static StrategyType of(String strategy){
        for(StrategyType hs : values()){
            if(hs.name().equalsIgnoreCase(strategy)){
                return hs;
            }
        }
        throw new IllegalArgumentException("invalid host select strategy : " + strategy);
    }
}
