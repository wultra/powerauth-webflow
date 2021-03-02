package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when one time password is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OtpNotFoundException extends NextStepServiceException {

    public static final String CODE = "OTP_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OtpNotFoundException(String message) {
        super(message);
    }

}
