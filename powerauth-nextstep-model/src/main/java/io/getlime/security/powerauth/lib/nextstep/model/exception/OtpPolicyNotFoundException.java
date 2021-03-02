package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when one time password policy is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OtpPolicyNotFoundException extends NextStepServiceException {

    public static final String CODE = "OTP_POLICY_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OtpPolicyNotFoundException(String message) {
        super(message);
    }

}
