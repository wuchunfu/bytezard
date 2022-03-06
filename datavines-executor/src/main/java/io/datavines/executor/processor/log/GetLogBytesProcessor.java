package io.datavines.executor.processor.log;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.datavines.common.utils.IOUtils;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.log.GetLogBytesRequestCommand;
import io.datavines.remote.command.log.GetLogBytesResponseCommand;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

public class GetLogBytesProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(GetLogBytesProcessor.class);

    @Override
    public void process(Channel channel, Command command) {

        Preconditions.checkArgument(
                CommandCode.GET_LOG_BYTES_REQUEST == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));

        GetLogBytesRequestCommand getLogBytesRequestCommand =
                JsonSerializer.deserialize(command.getBody(),GetLogBytesRequestCommand.class);
        GetLogBytesResponseCommand getLogBytesResponseCommand = new GetLogBytesResponseCommand();
        getLogBytesResponseCommand.setMsg(getFileContentBytes(getLogBytesRequestCommand.getPath()));
        channel.writeAndFlush(getLogBytesResponseCommand.convert2Command(command.getOpaque()));
    }

    /**
     * get files content bytes，for down load file
     *
     * @param filePath file path
     * @return byte array of file
     * @throws Exception exception
     */
    private byte[] getFileContentBytes(String filePath) {
        InputStream in = null;
        ByteArrayOutputStream bos = null;
        try {
            in = new FileInputStream(filePath);
            bos  = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error("get file bytes error",e);
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(in);
        }
        return new byte[0];
    }
}
