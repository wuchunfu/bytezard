package io.datavines.remote.codec;

import io.datavines.remote.command.Command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<Command> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
        if(msg == null){
            throw new Exception("encode msg is null");
        }
        out.writeByte(Command.MAGIC);
        out.writeByte(msg.getCode().ordinal());
        out.writeLong(msg.getOpaque());
        out.writeInt(msg.getBody().length);
        out.writeBytes(msg.getBody());
    }
}
