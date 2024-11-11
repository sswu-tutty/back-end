package com.example.tutty.exception;

public class CustomErrorResponse {
    private String error;
    private String message;

    public CustomErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    // Getters and Setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
