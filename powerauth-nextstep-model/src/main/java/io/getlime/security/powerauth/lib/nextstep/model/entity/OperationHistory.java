package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Class representing operation history entities.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
public class OperationHistory {

    @NotNull
    private AuthMethod authMethod;
    @NotNull
    private AuthResult authResult;
    @NotNull
    private AuthStepResult requestAuthStepResult;
    @Size(min = 2, max = 256)
    private String authStepResultDescription;
    @NotNull
    private boolean mobileTokenActive;
    @Size(min = 1, max = 256)
    private String powerAuthOperationId;
    private PAAuthenticationContext paAuthenticationContext;

}
