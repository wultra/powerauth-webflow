package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when one time password policy already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OtpPolicyAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "OTP_POLICY_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OtpPolicyAlreadyExistsException(String message) {
        super(message);
    }

}
