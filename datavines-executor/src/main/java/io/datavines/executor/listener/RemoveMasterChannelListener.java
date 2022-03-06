package io.datavines.executor.listener;

import io.datavines.common.utils.ThreadUtils;
import io.datavines.executor.cache.JobResponseCacheProcessor;
import io.datavines.remote.DataVinesRemoteClient;
import io.datavines.remote.command.RequestClientType;
import io.datavines.remote.connection.ServerConnectionManager;
import io.datavines.remote.connection.ServerReConnector;
import io.datavines.remote.listener.BaseHandlerListener;
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
    public void channelInactive(ChannelHandlerContext ctx, DataVinesRemoteClient mangerRemoteClient) throws Exception {
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
