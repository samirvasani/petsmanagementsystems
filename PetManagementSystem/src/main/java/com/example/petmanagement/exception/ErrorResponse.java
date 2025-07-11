package com.example.petmanagement.exception;

public record ErrorResponse(
        int status,
        String message,
        long timestamp
) {}
