package io.datavines.coordinator.server.dispatch;

import io.datavines.coordinator.config.CoordinatorConfiguration;
import io.datavines.coordinator.exception.ExecuteJobException;
import io.datavines.common.utils.ThreadUtils;
import io.datavines.coordinator.server.channel.ClientChannel;
import io.datavines.coordinator.server.channel.ClientChannelManager;
import io.datavines.coordinator.server.context.ExecutionContext;
import io.datavines.remote.command.Command;
import io.datavines.remote.utils.ChannelUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
public class JobDispatcherImpl implements IJobDispatcher<ExecuteResult> {

    private static final Logger logger = LoggerFactory.getLogger(JobDispatcherImpl.class);

    private final ClientChannelManager clientChannelManager;

    private final ExecutorSelector executorSelector;

    public JobDispatcherImpl(CoordinatorConfiguration configuration){
        executorSelector = new ExecutorSelector(configuration);
        clientChannelManager = ClientChannelManager.getInstance();
    }

    @Override
    public void beforeExecute(ExecutionContext executeContext) throws ExecuteJobException {

    }

    @Override
    public ExecuteResult execute(ExecutionContext context) throws ExecuteJobException {
        logger.info("execute job {}",context.getCommand());
        //重试策略
        return doExecute(context);
    }

    @Override
    public ExecuteResult execute(ExecutionContext context, ClientChannel clientChannel) throws ExecuteJobException {

        int retryCount = 3;
        boolean success = false;
        Command command = context.getCommand();
        if(clientChannel != null){
            Channel channel = clientChannel.getChannel();
            do {
                if(channel != null){
                    if(channel.isActive()){
                        channel.writeAndFlush(context.getCommand());
                        success = true;
                        logger.info(String.format("send command : %s to %s success", command, ChannelUtils.toAddress(channel)));
                    }else{
                        logger.info(String.format("send command : %s to %s error", command, ChannelUtils.toAddress(channel)));
                        clientChannelManager.removeClientChannel(context.getRequestClientType(),clientChannel);
                        retryCount--;
                        ThreadUtils.sleep(100);
                    }
                }else{
                    logger.info(" channel is null");
                }

            } while (retryCount >= 0 && !success);

            if (!success) {
                throw new ExecuteJobException(String.format("send command : %s to %s error", command, ChannelUtils.toAddress(channel)));
            }

            return new ExecuteResult(true,channel);
        }

        return new ExecuteResult(false,null);

    }

    @Override
    public void executeDirectly(ExecutionContext context) throws ExecuteJobException {

    }

    @Override
    public void afterExecute(ExecutionContext context) throws ExecuteJobException {

    }

    /**
     * execute logic
     * @param context context
     * @throws ExecuteJobException if error throws ExecuteJobException
     */
    private ExecuteResult doExecute(ExecutionContext context) throws ExecuteJobException {

        int retryCount = 3;
        boolean success = false;
        Command command = context.getCommand();
        ClientChannel clientChannel = null;
        Channel channel = null;

        int getClientsRetryNums = 3;
        while(getClientsRetryNums > 0){
            try{
                clientChannel = executorSelector
                        .select(clientChannelManager.getExecuteNodes(context.getRequestClientType()));
                channel = clientChannel.getChannel();
                break;
            }catch (Exception e){
                getClientsRetryNums--;
                logger.info("client list is empty, wait and retry");
                ThreadUtils.sleep(2000);
                if(getClientsRetryNums == 0){
                    throw new ExecuteJobException("get client error");
                }
            }
        }

        do {
            if(channel != null){
                if(channel.isActive()){
                    channel.writeAndFlush(context.getCommand());
                    success = true;
                    logger.info(String.format("send command : %s to %s success", command, ChannelUtils.toAddress(channel)));
                }else{
                    logger.info(String.format("send command : %s to %s error", command, ChannelUtils.toAddress(channel)));
                    clientChannelManager.removeClientChannel(context.getRequestClientType(),clientChannel);
                    retryCount--;
                    ThreadUtils.sleep(2000);
                }
            }else{
                logger.info(" channel is null");
            }

        } while (retryCount >= 0 && !success);

        if (!success) {
            throw new ExecuteJobException(String.format("send command : %s to %s error", command, ChannelUtils.toAddress(channel)));
        }

        return new ExecuteResult(true,channel);
    }

}
