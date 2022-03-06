package io.datavines.remote.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.future.ResponseFuture;
import io.datavines.remote.future.ResponseFutureManager;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.ChannelUtils;
import io.datavines.remote.utils.Constants;
import io.datavines.remote.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author zixi0825
 */
public class NettyBaseHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(NettyBaseHandler.class);

    protected ConcurrentHashMap<CommandCode, Pair<NettyEventProcessor,ExecutorService>> processors = new ConcurrentHashMap<>();

    protected ExecutorService callbackExecutor;

    /**
     *  default executor
     */
    protected final ExecutorService defaultExecutor = Executors.newFixedThreadPool(Constants.CPUS);

    protected void processMsg(final Channel channel, final Command command){
        logger.info(command.getCode()+"");
        ResponseFuture future = ResponseFutureManager.getResponseFuture(command.getOpaque());
        if(future != null){
            future.setResponseCommand(command);
            future.release();
            if(future.getInvokeCallback() != null) {
                this.callbackExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        future.executeInvokeCallback();
                    }
                });
            } else {
                future.putResponse(command);
                ResponseFutureManager.removeResponseFuture(command.getOpaque());
            }
        } else {
            processReceivedCommand(channel, command);
        }

    }

    private void processReceivedCommand(final Channel channel, final Command command) {
        final Pair<NettyEventProcessor,ExecutorService> pair = processors.get(command.getCode());
        if(pair != null) {
            Runnable runnable = () ->{
                try{
                    pair.getLeft().process(channel,command);
                }catch (Throwable e){
                    logger.error(String.format("process command %s exception", command), e);
                }
            };

            try{
                pair.getRight().submit(runnable);
            }catch (RejectedExecutionException e){
                logger.warn("thread pool is full, discard command {} from {}", command, ChannelUtils.getRemoteAddress(channel));
            }
        } else {
            logger.warn("receive response {}, but not matched any request ", command);
        }
    }

    public void registerProcessor(final CommandCode commandCode,
                                  final NettyEventProcessor processor) {
        this.registerProcessor(commandCode,processor,null);
    }

    public void registerProcessor(final CommandCode commandCode,
                                  final NettyEventProcessor processor,
                                  final ExecutorService executor) {
        ExecutorService ref = executor;
        if(executor == null) {
            ref = defaultExecutor;
        }

        processors.putIfAbsent(commandCode,Pair.of(processor,ref));
    }

}
