/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAliasStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class represents details of a user alias detail.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
@EqualsAndHashCode(of = "aliasName")
public class UserAliasDetail {

    @NotBlank
    @Size(min = 2, max = 256)
    private String aliasName;
    @Size(min = 2, max = 256)
    private String aliasValue;
    @NotNull
    private UserAliasStatus userAliasStatus;
    @NotNull
    private final Map<String, Object> extras = new LinkedHashMap<>();
    @NotNull
    private Date timestampCreated;
    private Date timestampLastUpdated;

}
