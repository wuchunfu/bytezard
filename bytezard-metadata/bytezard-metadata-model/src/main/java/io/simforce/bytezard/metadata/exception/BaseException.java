/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.simforce.bytezard.metadata.exception;

import java.util.List;

import io.simforce.bytezard.metadata.ErrorCode;

/**
 * Base Exception class for  API.
 */
public class BaseException extends Exception {

    private ErrorCode errorCode;

    public BaseException(ErrorCode errorCode, String ... params) {
        super(errorCode.getFormattedErrorMessage(params));
        this.errorCode = errorCode;
    }

    public BaseException(final ErrorCode errorCode, final List<String> params) {
        super(errorCode.getFormattedErrorMessage(params.toArray(new String[params.size()])));
        this.errorCode = errorCode;
    }

    public BaseException() {
        this(ErrorCode.INTERNAL_ERROR);
    }

    public BaseException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public BaseException(ErrorCode errorCode, Throwable cause, String... params) {
        super(errorCode.getFormattedErrorMessage(params), cause);
        this.errorCode = errorCode;
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public BaseException(Throwable cause) {
        super(cause);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public BaseException(ErrorCode errorCode, Throwable cause, boolean enableSuppression,
                         boolean writableStackTrace, String ... params) {
        super(errorCode.getFormattedErrorMessage(params), cause, enableSuppression, writableStackTrace);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public BaseException(String message, Throwable cause, boolean enableSuppression,
                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public BaseException(final ErrorCode errorCode, Throwable cause, final List<String> params) {
        super(errorCode.getFormattedErrorMessage(params.toArray(new String[params.size()])), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
