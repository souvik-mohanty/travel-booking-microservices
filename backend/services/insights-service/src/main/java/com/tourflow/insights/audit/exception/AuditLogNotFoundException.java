package com.tourflow.insights.audit.exception;

public class AuditLogNotFoundException extends RuntimeException {

    public AuditLogNotFoundException(String message) {
        super(message);
    }
}
