package com.hokyozu.kyofuse.shared.exception;

import java.time.Duration;

public class TooManyAttemptsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyAttemptsException(String message, Duration retryAfter) {
        super(message);
        this.retryAfterSeconds = Math.max(1, retryAfter.toSeconds());
    }

    public TooManyAttemptsException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
