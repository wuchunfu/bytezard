package io.simforce.bytezard.coordinator.server.dispatch.strategy;

import java.util.Collection;

/**
 * @author zixi0825
 */
public interface IExecutorSelectStrategy<T> {

    /**
     * select executor
     * @param list
     * @return
     * @throws Exception
     */
    T select(Collection<T> list) throws Exception;
}
