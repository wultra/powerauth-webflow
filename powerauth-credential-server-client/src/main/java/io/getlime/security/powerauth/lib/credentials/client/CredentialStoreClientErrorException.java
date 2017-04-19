package io.getlime.security.powerauth.lib.credentials.client;

import io.getlime.security.powerauth.lib.credentials.model.entity.ErrorModel;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class CredentialStoreClientErrorException extends Throwable {

    private ErrorModel error;

    public CredentialStoreClientErrorException() {
    }

    public CredentialStoreClientErrorException(Throwable cause) {
        super(cause);
    }

    public CredentialStoreClientErrorException(Throwable cause, ErrorModel error) {
        super(cause);
        this.error = error;
    }

    public ErrorModel getError() {
        return error;
    }
}
