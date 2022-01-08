package io.simforce.bytezard.common.entity;

/**
 * @author zixi0825
 */
public class LogResult {

    private String msg;

    private int offsetLine;

    public LogResult(String msg, int offsetLine){
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
