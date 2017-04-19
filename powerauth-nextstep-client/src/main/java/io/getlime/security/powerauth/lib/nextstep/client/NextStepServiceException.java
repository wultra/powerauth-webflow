package io.getlime.security.powerauth.lib.nextstep.client;

import io.getlime.security.powerauth.lib.nextstep.model.entity.ErrorModel;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class NextStepServiceException extends Throwable {

    private ErrorModel error;

    public NextStepServiceException(Throwable cause) {
        super(cause);
    }

    public NextStepServiceException(Throwable cause, ErrorModel error) {
        super(cause);
        this.error = error;
    }

    public ErrorModel getError() {
        return error;
    }
}
