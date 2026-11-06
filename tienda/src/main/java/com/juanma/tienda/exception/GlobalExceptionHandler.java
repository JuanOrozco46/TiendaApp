package com.juanma.tienda.exception;

import com.juanma.tienda.dto.HttpGlobalResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        HttpGlobalResponseDTO<Object> response = new HttpGlobalResponseDTO<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleBadRequest(BadRequestException ex) {
        HttpGlobalResponseDTO<Object> response = new HttpGlobalResponseDTO<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleUnauthorized(UnauthorizedException ex) {
        HttpGlobalResponseDTO<Object> response = new HttpGlobalResponseDTO<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleForbidden(ForbiddenException ex) {
        HttpGlobalResponseDTO<Object> response = new HttpGlobalResponseDTO<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleConflict(ConflictException ex) {
        HttpGlobalResponseDTO<Object> response = new HttpGlobalResponseDTO<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        HttpGlobalResponseDTO<Map<String, String>> response = new HttpGlobalResponseDTO<>("Errores de validación en la solicitud", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleGeneralException(Exception ex) {
        HttpGlobalResponseDTO<Object> response = new HttpGlobalResponseDTO<>("Error interno del servidor: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
