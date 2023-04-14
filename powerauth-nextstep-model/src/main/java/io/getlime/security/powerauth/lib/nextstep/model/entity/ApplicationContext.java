/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Application context for OAuth 2.0 consent screen.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class ApplicationContext {

    @NotBlank
    @Size(min = 1, max = 256)
    private String id;
    @NotBlank
    @Size(min = 2, max = 256)
    private String name;
    @Size(min = 2, max = 256)
    private String description;
    @NotNull
    private final List<String> originalScopes = new ArrayList<>();
    @NotNull
    private final Map<String, Object> extras = new LinkedHashMap<>();

}
