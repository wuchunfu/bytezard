package io.simforce.bytezard.executor.processor.log;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.log.RollViewLogRequestCommand;
import io.simforce.bytezard.remote.command.log.RollViewLogResponseCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.FastJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
public class LogRollViewProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(LogRollViewProcessor.class);

    @Override
    public void process(Channel channel, Command command) {

        Preconditions.checkArgument(CommandCode.ROLL_VIEW_LOG_REQUEST == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));

        RollViewLogRequestCommand rollViewLogRequestCommand =
                FastJsonSerializer.deserialize(command.getBody(),RollViewLogRequestCommand.class);
        List<String> contents = readPartFileContent(rollViewLogRequestCommand.getPath(),
                rollViewLogRequestCommand.getSkipLineNum(),
                rollViewLogRequestCommand.getLimit());
        StringBuilder msg = new StringBuilder();
        int offsetLine =  rollViewLogRequestCommand.getSkipLineNum();

        if (CollectionUtils.isNotEmpty(contents)) {
            for (String line:contents) {
                msg.append(line).append("\r\n");
            }
            offsetLine = rollViewLogRequestCommand.getSkipLineNum()+contents.size();
        }

        RollViewLogResponseCommand rollViewLogResponseCommand = new RollViewLogResponseCommand();
        rollViewLogResponseCommand.setMsg(msg.toString());
        rollViewLogResponseCommand.setOffsetLine(offsetLine);

        channel.writeAndFlush(rollViewLogResponseCommand.convert2Command(command.getOpaque()));
    }

    /**
     * read part file content，can skip any line and read some lines
     *
     * @param filePath file path
     * @param skipLine skip line
     * @param limit read lines limit
     * @return part file content
     */
    private List<String> readPartFileContent(String filePath,
                                             int skipLine,
                                             int limit){
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            return stream.skip(skipLine).limit(limit).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("read file error",e);
        }
        return Collections.emptyList();
    }
}
