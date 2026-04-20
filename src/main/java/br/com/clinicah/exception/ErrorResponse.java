package br.com.clinicah.exception;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
    private final int status;
    private final String message;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private List<String> errors;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(int status, String message, List<String> errors) {
        this(status, message);
        this.errors = errors;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<String> getErrors() { return errors; }
}
