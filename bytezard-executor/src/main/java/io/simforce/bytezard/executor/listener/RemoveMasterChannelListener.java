package io.simforce.bytezard.executor.listener;

import io.simforce.bytezard.common.utils.ThreadUtils;
import io.simforce.bytezard.executor.cache.JobResponseCacheProcessor;
import io.simforce.bytezard.remote.BytezardRemoteClient;
import io.simforce.bytezard.remote.command.RequestClientType;
import io.simforce.bytezard.remote.connection.ServerConnectionManager;
import io.simforce.bytezard.remote.connection.ServerReConnector;
import io.simforce.bytezard.remote.listener.BaseHandlerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

public class RemoveMasterChannelListener extends BaseHandlerListener {

    private final Logger logger = LoggerFactory.getLogger(RemoveMasterChannelListener.class);

    private final ServerReConnector serverReConnector;

    private final ServerConnectionManager serverConnectionManager;

    public RemoveMasterChannelListener(ServerReConnector serverReConnector){
        this.serverReConnector = serverReConnector;
        this.serverConnectionManager = ServerConnectionManager.getInstance();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx, BytezardRemoteClient mangerRemoteClient) throws Exception {
        logger.info("executor 2 coordinator channel inactive");
        serverConnectionManager.setConnection(null);
        serverReConnector.doConnect(mangerRemoteClient, () -> {

            ThreadUtils.sleep(2000);
            JobResponseCacheProcessor jobResponseCacheProcessor =
                    new JobResponseCacheProcessor();
            jobResponseCacheProcessor.start();
        }, RequestClientType.EXECUTOR);
    }

}
