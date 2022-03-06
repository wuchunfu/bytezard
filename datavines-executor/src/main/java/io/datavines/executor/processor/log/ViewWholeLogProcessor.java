package io.datavines.executor.processor.log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import io.datavines.common.utils.IOUtils;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.log.ViewLogRequestCommand;
import io.datavines.remote.command.log.ViewLogResponseCommand;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

public class ViewWholeLogProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(ViewWholeLogProcessor.class);

    @Override
    public void process(Channel channel, Command command) {

        Preconditions.checkArgument(CommandCode.VIEW_WHOLE_LOG_REQUEST == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));

        ViewLogRequestCommand rollViewLogRequestCommand =
                JsonSerializer.deserialize(command.getBody(),ViewLogRequestCommand.class);

        ViewLogResponseCommand viewLogResponseCommand = new ViewLogResponseCommand();
        viewLogResponseCommand.setMsg(readWholeFileContent(rollViewLogRequestCommand.getPath()));
        channel.writeAndFlush(viewLogResponseCommand.convert2Command(command.getOpaque()));
    }

    /**
     * read whole file content
     *
     * @param filePath file path
     * @return whole file content
     */
    private String readWholeFileContent(String filePath) {
        BufferedReader br = null;
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            while ((line = br.readLine()) != null){
                sb.append(line).append("\r\n");
            }
            return sb.toString();
        } catch (IOException e) {
            logger.error("read file error",e);
        } finally {
            IOUtils.closeQuietly(br);
        }
        return "";
    }
}
