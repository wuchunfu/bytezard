package io.simforce.bytezard.remote.codec;

import java.util.List;

import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.CommandHeader;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {

    private final CommandHeader commandHeader = new CommandHeader();

    public MessageDecoder(){
        super(State.MAGIC);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()){
            case MAGIC:
                checkMagic(in.readByte());
                checkpoint(State.COMMAND_CODE);
                break;
            case COMMAND_CODE:
                commandHeader.setCode(in.readByte());
                checkpoint(State.OPAQUE);
                break;
            case OPAQUE:
                commandHeader.setOpaque(in.readLong());
                checkpoint(State.BODY_LENGTH);
                break;
            case BODY_LENGTH:
                commandHeader.setBodyLength(in.readInt());
                checkpoint(State.BODY);
                break;
            case BODY:
                byte[] body = new byte[commandHeader.getBodyLength()];
                in.readBytes(body);
                Command packet = new Command();
                packet.setCode(getCommandCode(commandHeader.getCode()));
                packet.setOpaque(commandHeader.getOpaque());
                packet.setBody(body);
                out.add(packet);
                checkpoint(State.MAGIC);
                break;
            default:
                throw new IllegalStateException("invalid state:" + state());
        }
    }

    private CommandCode getCommandCode(byte code){
        for(CommandCode cd:CommandCode.values()){
            if(cd.ordinal() == code){
                return  cd;
            }
        }
        return null;
    }

    private void checkMagic(byte magic){
        if(magic != Command.MAGIC){
            throw  new IllegalArgumentException("illegal packet {magic}" + magic);
        }
    }

    enum State{
        /**
         * magic
         */
        MAGIC,
        /**
         * command code
         */
        COMMAND_CODE,
        /**
         * opaque
         */
        OPAQUE,
        /**
         * body length
         */
        BODY_LENGTH,
        /**
         * body
         */
        BODY;
    }
}
