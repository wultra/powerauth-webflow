package io.getlime.security.powerauth.app.webauth.exception;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class NextStepServiceException extends Throwable {

    public NextStepServiceException() {
    }

    public NextStepServiceException(String message) {
        super(message);
    }

    public NextStepServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NextStepServiceException(Throwable cause) {
        super(cause);
    }

    public NextStepServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
