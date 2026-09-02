package com.smartcrop.ai.service;

public class GroqRateLimitException extends RuntimeException {
    public GroqRateLimitException() {
        super("Groq is temporarily rate limited");
    }
}
