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
