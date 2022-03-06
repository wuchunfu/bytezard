package io.datavines.remote.command;

import java.io.Serializable;

public class CommandHeader implements Serializable {

    private byte code;
    private long opaque;
    private int bodyLength;

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public long getOpaque() {
        return opaque;
    }

    public void setOpaque(long opaque) {
        this.opaque = opaque;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }
}
