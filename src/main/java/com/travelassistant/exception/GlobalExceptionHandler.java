package com.travelassistant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.travelassistant.dto.ConversationResponse;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException ex) {
        // Just return 404 without logging an error for missing static resources
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ConversationResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ConversationResponse errorResponse = new ConversationResponse(
                "I'm sorry, an unexpected error occurred. Please try again later.",
                null,
                true);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ConversationResponse> handleExternalServiceException(ExternalServiceException ex) {
        log.error("External service error", ex);
        ConversationResponse errorResponse = new ConversationResponse(
                "I'm having trouble connecting to external services. Some information might be limited.",
                null,
                true);
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
