package com.tcgm.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Conflit : %s avec %s : '%s' existe déjà", resourceName, fieldName, fieldValue));
    }
}