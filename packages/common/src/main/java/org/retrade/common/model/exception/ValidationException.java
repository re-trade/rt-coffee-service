package org.retrade.common.model.exception;

import org.retrade.common.model.dto.response.ResponseObject;

public class ValidationException extends BaseException{
    public ValidationException(String message) {
        super(message);
        errors = new ResponseObject.Builder<String>()
                .success(false)
                .messages(message)
                .code("VALIDATION_ERROR")
                .build();
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        errors = new ResponseObject.Builder<String>()
                .success(false)
                .messages(message)
                .code("VALIDATION_ERROR")
                .build();
    }
    public ValidationException(String... messages) {
        super(messages[0]);
        errors = new ResponseObject.Builder<String>()
                .success(false)
                .messages(messages)
                .code("VALIDATION_ERROR")
                .build();
    }
}
