package com.example.tutty.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CustomErrorResponse handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return new CustomErrorResponse("User not found", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CustomErrorResponse handleAuthenticationException(AuthenticationException ex) {
        return new CustomErrorResponse("Authentication failed", "Invalid username or password.");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomErrorResponse handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return new CustomErrorResponse("User already exists", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CustomErrorResponse handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return new CustomErrorResponse("Login failed", ex.getMessage());
    }
}
