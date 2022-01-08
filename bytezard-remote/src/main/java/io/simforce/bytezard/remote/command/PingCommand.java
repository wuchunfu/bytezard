package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class PingCommand extends BaseCommand {

    private RequestClientType clientType;

    public PingCommand(){
        this.commandCode = CommandCode.PING;
    }

    public RequestClientType getClientType() {
        return clientType;
    }

    public void setClientType(RequestClientType clientType) {
        this.clientType = clientType;
    }
}
