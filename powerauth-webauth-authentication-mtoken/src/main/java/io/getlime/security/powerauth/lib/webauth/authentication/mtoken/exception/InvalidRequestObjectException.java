package io.getlime.security.powerauth.lib.webauth.authentication.mtoken.exception;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class InvalidRequestObjectException extends Exception {

    public InvalidRequestObjectException() {
        super("Invalid request object sent to Mobile Token API component.");
    }
}
