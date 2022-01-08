package io.simforce.bytezard.remote.command;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class Command implements Serializable {

    private static final AtomicLong REQUEST_ID = new AtomicLong(1);

    public static final byte MAGIC = (byte) 0xbabe;

    public Command(){
        this.opaque = REQUEST_ID.incrementAndGet();
    }

    public Command(long opaque){
        this.opaque = opaque;
    }

    private CommandCode code;

    private long opaque;

    private byte[] body;

    public CommandCode getCode() {
        return code;
    }

    public void setCode(CommandCode code) {
        this.code = code;
    }

    public long getOpaque() {
        return opaque;
    }

    public void setOpaque(long opaque) {
        this.opaque = opaque;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (opaque ^ (opaque >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Command other = (Command) obj;
        return opaque == other.opaque;
    }
}
