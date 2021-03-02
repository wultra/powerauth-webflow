package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when generation algorithm for OTP is not supported.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OtpGenAlgorithmNotSupportedException extends NextStepServiceException {

    public static final String CODE = "OTP_GEN_ALGORITHM_NOT_SUPPORTED";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OtpGenAlgorithmNotSupportedException(String message) {
        super(message);
    }

}
