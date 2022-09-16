package io.quarkus.devui.deployment.spi;

public class InvalidNamingException extends RuntimeException {

    public InvalidNamingException() {
    }

    public InvalidNamingException(String message) {
        super(message);
    }

    public InvalidNamingException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidNamingException(Throwable cause) {
        super(cause);
    }

    public InvalidNamingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
