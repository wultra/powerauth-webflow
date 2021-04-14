package io.getlime.security.powerauth.lib.nextstep.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Class representing information about third party.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
@AllArgsConstructor
public class PartyInfo {

    @Size(min = 2, max = 256)
    private String logoUrl;
    @NotBlank
    @Size(min = 2, max = 256)
    private String name;
    @Size(min = 2, max = 256)
    private String description;
    @Size(min = 2, max = 256)
    private String websiteUrl;

}
