package io.simforce.bytezard.remote.command;

import java.io.Serializable;

import io.simforce.bytezard.remote.utils.FastJsonSerializer;

public class BaseCommand implements Serializable {

    protected CommandCode commandCode = CommandCode.DEFAULT;

    /**
     * package response command
     * @return command
     */
    public Command convert2Command(){
        Command command = new Command();
        return getCommand(command);
    }

    public Command convert2Command(long opaque){
        Command command = new Command(opaque);
        return getCommand(command);
    }

    private Command getCommand(Command command) {
        command.setCode(commandCode);
        byte[] body = FastJsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }
}
