package org.retrade.voucher.controller;

import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.AuthException;
import org.retrade.common.model.exception.BaseException;
import org.retrade.common.model.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ApplicationControllerAdvice {
    @ExceptionHandler(value = {
            AuthException.class,
            ActionFailedException.class,
            ValidationException.class
    })
    public ResponseEntity<ResponseObject<String>> applicationException(BaseException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(exception.getErrors());
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseObject<String>> accessDeniedException(AccessDeniedException exception) {
        var responseError = new ResponseObject.Builder<String>()
                .success(false)
                .messages(exception.getMessage())
                .code("AUTH_FAILED")
                .content(null)
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseError);
    }
    @ExceptionHandler({
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            LockedException.class,
            DisabledException.class,
            AccountStatusException.class,
            InsufficientAuthenticationException.class
    })
    public ResponseEntity<ResponseObject<String>> authenticationFailedException(AuthenticationException exception) {
        var responseError = new ResponseObject.Builder<String>()
                .success(false)
                .messages(exception.getMessage())
                .code("AUTH_FAILED")
                .content(null)
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseObject<String>> somethingWrongException(Exception ex) {
        log.error("SOMETHING WRONG",ex);
        var responseError = new ResponseObject.Builder<String>()
                .success(false)
                .messages(ex.getMessage())
                .code("SOMETHING_WRONG")
                .content(null)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
    }
}
