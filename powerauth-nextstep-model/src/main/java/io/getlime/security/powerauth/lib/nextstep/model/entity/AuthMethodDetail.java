/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
