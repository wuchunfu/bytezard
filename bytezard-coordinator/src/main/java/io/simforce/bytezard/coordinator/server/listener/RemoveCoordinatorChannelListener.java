package io.simforce.bytezard.coordinator.server.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.simforce.bytezard.remote.BytezardRemoteClient;
import io.simforce.bytezard.remote.command.RequestClientType;
import io.simforce.bytezard.remote.connection.ServerConnectionManager;
import io.simforce.bytezard.remote.connection.ServerReConnector;
import io.simforce.bytezard.remote.listener.BaseHandlerListener;

public class RemoveCoordinatorChannelListener extends BaseHandlerListener {

    private final Logger logger = LoggerFactory.getLogger(RemoveCoordinatorChannelListener.class);

    private final ServerReConnector serverReConnector;
    private final ServerConnectionManager serverConnectionManager;

    public RemoveCoordinatorChannelListener(ServerReConnector serverReConnector){
        this.serverReConnector = serverReConnector;
        this.serverConnectionManager = ServerConnectionManager.getInstance();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx, BytezardRemoteClient bytezardRemoteClient) throws Exception {
        logger.info("executor 2 coordinator channel inactive");
        serverConnectionManager.setConnection(null);
        serverReConnector.doConnect(bytezardRemoteClient,null, RequestClientType.CLIENT);
    }

}
