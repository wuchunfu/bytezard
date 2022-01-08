package io.simforce.bytezard.remote.future;

/**
 * @author zixi0825
 */
public interface InvokeCallback {

    /**
     *  operation
     *
     * @param responseFuture responseFuture
     */
    void operationComplete(final ResponseFuture responseFuture);
}
