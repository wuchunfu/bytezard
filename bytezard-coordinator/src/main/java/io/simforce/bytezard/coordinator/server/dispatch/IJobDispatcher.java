package io.simforce.bytezard.coordinator.server.dispatch;

import io.simforce.bytezard.coordinator.exception.ExecuteJobException;
import io.simforce.bytezard.coordinator.server.channel.ClientChannel;
import io.simforce.bytezard.coordinator.server.context.ExecutionContext;

/**
 * @author zixi0825
 */
public interface IJobDispatcher<T> {

    /**
     * before execute
     * @param executeContext
     * @throws ExecuteJobException
     */
    void beforeExecute(ExecutionContext executeContext) throws ExecuteJobException;

    /**
     * execute job
     * @param context context
     * @return T
     * @throws ExecuteJobException if error throws ExecuteJobException
     */
    T execute(ExecutionContext context) throws ExecuteJobException;

    /**
     * execute job
     * @param context context
     * @return T
     * @throws ExecuteJobException if error throws ExecuteJobException
     */
    T execute(ExecutionContext context, ClientChannel channel) throws ExecuteJobException;

    /**
     * execute job directly without retry
     * @param context context
     * @throws ExecuteJobException if error throws ExecuteJobException
     */
    void executeDirectly(ExecutionContext context) throws ExecuteJobException;

    /**
     *  after execute
     * @param context context
     * @throws ExecuteJobException if error throws ExecuteJobException
     */
    void afterExecute(ExecutionContext context) throws ExecuteJobException;
}
