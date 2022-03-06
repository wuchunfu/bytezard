package io.datavines.remote.command.log;

import io.datavines.remote.command.BaseCommand;
import io.datavines.remote.command.CommandCode;

/**
 * @author zixi0825
 */
public class RollViewLogResponseCommand extends BaseCommand {

    /**
     *  log path
     */
    private String msg;

    private int offsetLine;

    public RollViewLogResponseCommand() {
        this.commandCode = CommandCode.ROLL_VIEW_LOG_RESPONSE;
    }

    public RollViewLogResponseCommand(String msg, int offsetLine) {
        this.commandCode = CommandCode.ROLL_VIEW_LOG_RESPONSE;
        this.msg = msg;
        this.offsetLine = offsetLine;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getOffsetLine() {
        return offsetLine;
    }

    public void setOffsetLine(int offsetLine) {
        this.offsetLine = offsetLine;
    }
}
