package io.datavines.coordinator.api.enums;

import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.Arrays;

public class DataVinesApiException extends RuntimeException {

    private Status status;

    public DataVinesApiException() {
        super();
    }

    public DataVinesApiException(String message) {
        super(message);
    }

    public DataVinesApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataVinesApiException(Throwable cause) {
        super(cause);
    }

    public DataVinesApiException(Status status) {
        super(status.getMsg());
        this.status = status;
    }

    public DataVinesApiException(Status status, Throwable cause) {
        super(status.getMsg(),cause);
        this.status = status;
    }

    public DataVinesApiException(Status status, Object... statusParams) {
        super(CollectionUtils.isEmpty(Arrays.asList(statusParams))?status.getZhMsg(): MessageFormat.format(status.getZhMsg(), statusParams));
        this.status = status;
    }

    protected DataVinesApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public Status getStatus() {
        return status;
    }
}
