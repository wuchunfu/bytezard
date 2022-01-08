package io.simforce.bytezard.remote.command.log;

import io.simforce.bytezard.remote.command.BaseCommand;
import io.simforce.bytezard.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class GetLogBytesResponseCommand extends BaseCommand {

    private byte[] msg;

    public GetLogBytesResponseCommand(){
        this.commandCode = CommandCode.GET_LOG_BYTES_RESPONSE;
    }

    public GetLogBytesResponseCommand(byte[] msg){
        this.msg = msg;
        this.commandCode = CommandCode.GET_LOG_BYTES_RESPONSE;
    }

    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }
}
