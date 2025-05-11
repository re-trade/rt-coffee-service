package org.retrade.common.model.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.dto.response.ResponseObject;

@Slf4j
@Getter
public abstract class BaseException extends RuntimeException {
    protected ResponseObject<String> errors;
    public BaseException(String message) {
        super(message);
        log.error(message);
    }
    public BaseException(String message, Throwable cause) {
        super(message, cause);
        log.info(message);
        log.error(message);
    }
}
