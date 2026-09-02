package com.smartcrop.advisory.service;

/**
 * Thrown when Groq advisory generation fails validation or encounters errors.
 * This exception prevents silent fallback to old hardcoded advisory systems.
 */
public class GroqAdvisoryGenerationException extends RuntimeException {

    public GroqAdvisoryGenerationException(String message) {
        super(message);
    }

    public GroqAdvisoryGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
