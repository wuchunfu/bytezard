package io.datavines.remote.future;

import io.datavines.remote.command.Command;

/**
 * @author zixi0825
 */
public interface InvokeFuture {

    Command waitResponse() throws InterruptedException;

    Command waitResponse(long timeoutMills) throws InterruptedException;

    void putResponse(Command command);

    void executeInvokeCallBack();

    void setCause(Throwable cause);

    Throwable getCase();

    InvokeCallback getInvokeCallback();

    void setInvokeCallback(InvokeCallback invokeCallback);

    boolean isDone();

}
