package com.example.car.sharing.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.stripe.exception.StripeException;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(this::getErrorMessage)
                .toList();
        ResponseBody responseBody = new ResponseBody(LocalDateTime.now(),
                HttpStatus.BAD_REQUEST, errors);
        return new ResponseEntity<>(responseBody, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ResponseBody responseBody = new ResponseBody(LocalDateTime.now(),
                HttpStatus.METHOD_NOT_ALLOWED, List.of(ex.getMessage()));
        return new ResponseEntity<>(responseBody, headers, status);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(
            EntityNotFoundException ex
    ) {
        ResponseBody responseBody = new ResponseBody(LocalDateTime.now(),
                HttpStatus.NOT_FOUND, List.of(ex.getMessage()));
        return new ResponseEntity<>(responseBody,
                HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<Object> handleStripeException(
            StripeException ex
    ) {
        ResponseBody responseBody = new ResponseBody(LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR, List.of(ex.getMessage()));
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Object> handleRegistrationException(
            RegistrationException ex
    ) {
        ResponseBody responseBody = new ResponseBody(LocalDateTime.now(),
                HttpStatus.CONFLICT, List.of(ex.getMessage()));
        return new ResponseEntity<>(responseBody,
                HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Object> handleJwtException(
            JwtException ex
    ) {
        ResponseBody responseBody = new ResponseBody(LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED, List.of(ex.getMessage()));
        return new ResponseEntity<>(responseBody,
                HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(
            UsernameNotFoundException ex
    ) {
        ResponseBody responseBody = new ResponseBody(LocalDateTime.now(),
                HttpStatus.FORBIDDEN, List.of(ex.getMessage()));
        return new ResponseEntity<>(responseBody,
                HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()));
    }

    private String getErrorMessage(ObjectError e) {
        if (e instanceof FieldError) {
            String field = ((FieldError) e).getField();
            String message = e.getDefaultMessage();
            return field + " " + message;
        }
        return e.getDefaultMessage();
    }

    private record ResponseBody(
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime timestamp, HttpStatus status,
            List<String> errors) {}
}
