package io.datavines.coordinator.server.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.datavines.remote.DataVinesRemoteClient;
import io.datavines.remote.command.RequestClientType;
import io.datavines.remote.connection.ServerConnectionManager;
import io.datavines.remote.connection.ServerReConnector;
import io.datavines.remote.listener.BaseHandlerListener;

public class RemoveCoordinatorChannelListener extends BaseHandlerListener {

    private final Logger logger = LoggerFactory.getLogger(RemoveCoordinatorChannelListener.class);

    private final ServerReConnector serverReConnector;
    private final ServerConnectionManager serverConnectionManager;

    public RemoveCoordinatorChannelListener(ServerReConnector serverReConnector){
        this.serverReConnector = serverReConnector;
        this.serverConnectionManager = ServerConnectionManager.getInstance();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx, DataVinesRemoteClient datavinesRemoteClient) throws Exception {
        logger.info("executor 2 coordinator channel inactive");
        serverConnectionManager.setConnection(null);
        serverReConnector.doConnect(datavinesRemoteClient,null, RequestClientType.CLIENT);
    }

}
