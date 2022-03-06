/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.datavines.remote.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.datavines.remote.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * response future
 */
public class ResponseFuture {

    private static final Logger logger = LoggerFactory.getLogger(ResponseFuture.class);

    /**
     *  request unique identification
     */
    private final long opaque;

    /**
     *  timeout
     */
    private final long timeoutMillis;

    /**
     *  invokeCallback function
     */
    private final InvokeCallback invokeCallback;

    /**
     *  releaseSemaphore
     */
    private final ReleaseSemaphore releaseSemaphore;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final long beginTimestamp = System.currentTimeMillis();

    /**
     *  response command
     */
    private Command responseCommand;

    private volatile boolean sendSuccess = true;

    private Throwable cause;

    public ResponseFuture(long opaque, long timeoutMillis, InvokeCallback invokeCallback, ReleaseSemaphore releaseSemaphore) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.releaseSemaphore = releaseSemaphore;
    }

    /**
     *  wait for response
     *
     * @return command
     * @throws InterruptedException
     */
    public Command waitResponse() throws InterruptedException {
        this.latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    /**
     *  put response
     *
     * @param responseCommand responseCommand
     */
    public void putResponse(final Command responseCommand) {
        this.responseCommand = responseCommand;
        this.latch.countDown();

    }

    /**
     *  whether timeout
     * @return timeout
     */
    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    /**
     *  execute invoke callback
     */
    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            invokeCallback.operationComplete(this);
        }
    }

    public boolean isSendSuccess() {
        return sendSuccess;
    }

    public void setSendSuccess(boolean sendSuccess) {
        this.sendSuccess = sendSuccess;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    public long getOpaque() {
        return opaque;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public Command getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(Command responseCommand) {
        this.responseCommand = responseCommand;
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    /**
     *  release
     */
    public void release() {
        if(this.releaseSemaphore != null){
            this.releaseSemaphore.release();
        }
    }

    @Override
    public String toString() {
        return "ResponseFuture{" +
                "opaque=" + opaque +
                ", timeoutMillis=" + timeoutMillis +
                ", invokeCallback=" + invokeCallback +
                ", releaseSemaphore=" + releaseSemaphore +
                ", latch=" + latch +
                ", beginTimestamp=" + beginTimestamp +
                ", responseCommand=" + responseCommand +
                ", sendSuccess=" + sendSuccess +
                ", cause=" + cause +
                '}';
    }

}
