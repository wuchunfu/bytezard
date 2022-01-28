package io.simforce.bytezard.coordinator.api.enums;

import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.Arrays;

public class BytezardApiException extends RuntimeException {

    private Status status;

    public BytezardApiException() {
        super();
    }

    public BytezardApiException(String message) {
        super(message);
    }

    public BytezardApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public BytezardApiException(Throwable cause) {
        super(cause);
    }

    public BytezardApiException(Status status) {
        super(status.getMsg());
        this.status = status;
    }

    public BytezardApiException(Status status, Throwable cause) {
        super(status.getMsg(),cause);
        this.status = status;
    }

    public BytezardApiException(Status status, Object... statusParams) {
        super(CollectionUtils.isEmpty(Arrays.asList(statusParams))?status.getZhMsg(): MessageFormat.format(status.getZhMsg(), statusParams));
        this.status = status;
    }

    protected BytezardApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public Status getStatus() {
        return status;
    }
}
