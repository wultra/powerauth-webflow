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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Request object used for creating a user alias.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CreateUserAliasRequest {

    @NotBlank
    @Size(min = 1, max = 256)
    private String userId;
    @NotBlank
    @Size(min = 2, max = 256)
    private String aliasName;
    @NotBlank
    @Size(min = 2, max = 256)
    private String aliasValue;

    @JsonSetter(nulls = Nulls.SKIP)
    private final Map<String, Object> extras = new LinkedHashMap<>();

}
