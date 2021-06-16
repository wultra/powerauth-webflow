package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

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
    @Positive
    private Long orderNumber;
    @NotNull
    private Boolean checkUserPrefs;
    @Positive
    private Integer userPrefsColumn;
    private Boolean userPrefsDefault;
    @NotNull
    private Boolean checkAuthFails;
    @Positive
    private Integer maxAuthFails;
    @NotNull
    private Boolean hasUserInterface;
    @NotNull
    private Boolean hasMobileToken;
    @Size(min = 1, max = 256)
    private String displayNameKey;

}
