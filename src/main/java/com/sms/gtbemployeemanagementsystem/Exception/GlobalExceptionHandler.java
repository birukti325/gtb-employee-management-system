package com.sms.gtbemployeemanagementsystem.Exception;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class GlobalExceptionHandler {

    public Map<String, Object> handleRuntimeException(RuntimeException ex) {
        return errorBody(ex.getMessage());
    }

    public Map<String, Object> handleException(Exception ex) {
        return errorBody("An unexpected error occurred: " + ex.getMessage());
    }

    private Map<String, Object> errorBody(String message) {
        return Map.of(
                "error", message,
                "timestamp", LocalDateTime.now().toString()
        );
    }
}