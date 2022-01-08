package io.simforce.bytezard.remote.command;

/**
 * @author zixi0825
 */
public class PongCommand extends BaseCommand {

    private RequestClientType clientType;

    public PongCommand(){
        this.commandCode = CommandCode.PONG;
    }

    public RequestClientType getClientType() {
        return clientType;
    }

    public void setClientType(RequestClientType clientType) {
        this.clientType = clientType;
    }
}
