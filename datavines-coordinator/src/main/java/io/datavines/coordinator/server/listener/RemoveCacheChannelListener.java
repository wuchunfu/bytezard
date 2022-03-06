package io.datavines.coordinator.server.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datavines.coordinator.server.cache.JobExecuteManager;
import io.netty.channel.ChannelHandlerContext;
import io.datavines.coordinator.server.channel.ClientChannel;
import io.datavines.coordinator.server.channel.ClientChannelManager;
import io.datavines.remote.listener.BaseHandlerListener;
import io.datavines.remote.utils.ChannelUtils;

public class RemoveCacheChannelListener extends BaseHandlerListener {

    private final Logger logger = LoggerFactory.getLogger(RemoveCacheChannelListener.class);

    private final ClientChannelManager clientChannelManager;

    private final JobExecuteManager jobExecuteManager;

    public RemoveCacheChannelListener(JobExecuteManager jobExecuteManager){
        this.clientChannelManager = ClientChannelManager.getInstance();
        this.jobExecuteManager = jobExecuteManager;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channel inactive");
        ClientChannel clientChannel = new ClientChannel();
        clientChannel.setHost(ChannelUtils.toAddress(ctx.channel()));
        clientChannel.setChannel(ctx.channel());
        clientChannelManager.removeChannel(clientChannel);

//        if(clientChannelManager.isExecutorChannelListEmpty()){
//            //发送信息通知运维人员
//        }

        jobExecuteManager.reassignJob(ChannelUtils.toAddress(ctx.channel()));
    }

}
