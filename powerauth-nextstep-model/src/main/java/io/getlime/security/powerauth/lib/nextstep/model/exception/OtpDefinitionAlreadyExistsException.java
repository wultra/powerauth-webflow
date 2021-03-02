package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when one time password definition already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OtpDefinitionAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "OTP_DEFINITION_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OtpDefinitionAlreadyExistsException(String message) {
        super(message);
    }

}
