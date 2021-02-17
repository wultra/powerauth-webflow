package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class represents state of an authentication method for given user.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class UserAuthMethodDetail {

    @NotNull
    private String userId;
    @NotNull
    private AuthMethod authMethod;
    @NotNull
    private Boolean hasUserInterface;
    private String displayNameKey;
    @NotNull
    private Boolean hasMobileToken;
    private final Map<String, String> config = new LinkedHashMap<>();

}
