package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

/**
 * Exception for states with invalid data.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class QRCodeInvalidDataException extends Exception {

    public QRCodeInvalidDataException() {
        super("QR Code could not be generated.");
    }

    public QRCodeInvalidDataException(String message) {
        super(message);
    }

}
