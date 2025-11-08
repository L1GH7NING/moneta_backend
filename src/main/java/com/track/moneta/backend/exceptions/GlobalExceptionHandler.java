package com.track.moneta.backend.exceptions;

import com.track.moneta.backend.payload.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> handleAPIException(APIException ex) {
        APIResponse response = new APIResponse(ex.getMessage(), false);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Grab the first field error
        String errorMessage = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getField() + " "
                + ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Validation error";

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse(errorMessage, false));
    }
}
