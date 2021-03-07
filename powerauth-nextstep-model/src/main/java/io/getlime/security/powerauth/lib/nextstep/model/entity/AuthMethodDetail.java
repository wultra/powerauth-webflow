package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Class represents details of an authentication method.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class AuthMethodDetail {

    @NotNull
    private AuthMethod authMethod;
    @NotNull
    private Boolean hasUserInterface;
    @NotNull
    private String displayNameKey;
    @NotNull
    private Boolean hasMobileToken;

}
