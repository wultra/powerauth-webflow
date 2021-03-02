package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when one time password definition is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OtpDefinitionNotFoundException extends NextStepServiceException {

    public static final String CODE = "OTP_DEFINITION_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OtpDefinitionNotFoundException(String message) {
        super(message);
    }

}
