package io.getlime.security.powerauth.lib.nextstep.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Class representing information about third party.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
@AllArgsConstructor
public class PartyInfo {

    @NotNull
    private String logoUrl;
    @NotNull
    private String name;
    private String description;
    private String websiteUrl;

}
