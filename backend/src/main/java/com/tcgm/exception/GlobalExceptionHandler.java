package com.tcgm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================================================
    // 1. ERREURS PERSONNALISÉES
    // =========================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("NOT_FOUND")
            .message(ex.getMessage())
            .errorCode("RNF-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        log.error("Bad request: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("BAD_REQUEST")
            .message(ex.getMessage())
            .errorCode("BRQ-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        log.error("Unauthorized: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("UNAUTHORIZED")
            .message(ex.getMessage())
            .errorCode("UNA-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex, WebRequest request) {
        log.error("Forbidden: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("FORBIDDEN")
            .message(ex.getMessage())
            .errorCode("FRB-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex, WebRequest request) {
        log.error("Conflict: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("CONFLICT")
            .message(ex.getMessage())
            .errorCode("CNF-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // =========================================================
    // 2. ERREURS DE VALIDATION
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation failed: {}", errors);
        ErrorResponse error = ErrorResponse.builder()
            .status("VALIDATION_ERROR")
            .message("Validation des données échouée")
            .errorCode("VAL-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .details(errors)
            .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // =========================================================
    // 3. ERREURS DE SÉCURITÉ
    // =========================================================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.error("Authentication error: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("UNAUTHORIZED")
            .message("Authentification échouée : email ou mot de passe incorrect")
            .errorCode("AUT-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("UNAUTHORIZED")
            .message("Identifiants invalides")
            .errorCode("AUT-002")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("FORBIDDEN")
            .message("Accès refusé - Vous n'avez pas les droits nécessaires")
            .errorCode("ACC-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // =========================================================
    // 4. ERREURS TECHNIQUES
    // =========================================================

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.error("Type mismatch: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("BAD_REQUEST")
            .message("Format de paramètre invalide : " + ex.getName())
            .errorCode("TYP-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("BAD_REQUEST")
            .message(ex.getMessage())
            .errorCode("ARG-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(
            NullPointerException ex, WebRequest request) {
        log.error("Null pointer: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .status("INTERNAL_ERROR")
            .message("Erreur interne du serveur")
            .errorCode("NPE-001")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================================================
    // 5. ERREUR GÉNÉRIQUE (Fallback)
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.builder()
            .status("INTERNAL_ERROR")
            .message("Une erreur inattendue s'est produite")
            .errorCode("ERR-500")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .details(ex.getMessage())
            .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}