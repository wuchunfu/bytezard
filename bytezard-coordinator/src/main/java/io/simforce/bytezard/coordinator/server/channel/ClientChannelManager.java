package io.simforce.bytezard.coordinator.server.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.remote.command.RequestClientType;
import io.simforce.bytezard.remote.utils.Host;

/**
 * 用于管理host，主要就是获取
 *
 * @author zixi0825
 */
public class ClientChannelManager {

    private final Logger logger = LoggerFactory.getLogger(ClientChannelManager.class);

    private final ConcurrentHashMap<Host,ClientChannel> executors =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Host,ClientChannel> clients =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String,ClientChannel> ip2Executors =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String,ClientChannel> ip2Clients =
            new ConcurrentHashMap<>();

    private ClientChannelManager(){}

    private static class Singleton{
        static ClientChannelManager instance = new ClientChannelManager();
    }

    public static ClientChannelManager getInstance(){
        return Singleton.instance;
    }

    /**
     * 根据client的类型获取当前可用的ClientChannel
     * @param executorType
     * @return
     */
    public List<ClientChannel> getExecuteNodes(RequestClientType executorType){

        switch (executorType){
            case EXECUTOR:
                return getClientChannels(executors);
            case CLIENT:
                return getClientChannels(clients);
            default:
                return new ArrayList<>();
        }

    }

    public void addClientChannel(RequestClientType requestClientType,
                                 ClientChannel clientChannel){
        if(RequestClientType.CLIENT == requestClientType){
            clients.put(clientChannel.getHost(),clientChannel);
            ip2Clients.put(clientChannel.getHost().getIp(),clientChannel);
        }else if(RequestClientType.EXECUTOR == requestClientType){
            executors.put(clientChannel.getHost(),clientChannel);
            ip2Executors.put(clientChannel.getHost().getIp(),clientChannel);
        }
    }

    private List<ClientChannel> getClientChannels(ConcurrentHashMap<Host,ClientChannel> clientMap){
        return new ArrayList<>(clientMap.values());
    }

    public void removeClientChannel(RequestClientType requestClientType,
                                    ClientChannel clientChannel){
        if(RequestClientType.CLIENT == requestClientType){
            clients.remove(clientChannel.getHost());
        }else if(RequestClientType.EXECUTOR == requestClientType){
            executors.remove(clientChannel.getHost());
        }
    }

    public void removeChannel(ClientChannel clientChannel){
        clients.remove(clientChannel.getHost());
        executors.remove(clientChannel.getHost());
    }

    public ClientChannel getChannel(Host host,RequestClientType requestClientType){
        if(RequestClientType.CLIENT == requestClientType){
            return clients.get(host);
        }else if(RequestClientType.EXECUTOR == requestClientType){
            return executors.get(host);
        }

        return null;
    }

    public ClientChannel getExecutorByIp(String ip){
        return ip2Executors.get(ip);
    }

    public ClientChannel getClientByIp(String ip){
        return ip2Clients.get(ip);
    }

    public boolean isExecutorChannelListEmpty(){
        return executors.isEmpty();
    }

    public boolean isClientChannelListEmpty(){
        return clients.isEmpty();
    }
}
