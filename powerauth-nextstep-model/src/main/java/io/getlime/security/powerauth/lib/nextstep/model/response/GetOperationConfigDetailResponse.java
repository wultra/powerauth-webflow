/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Response object used for getting the operation configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class GetOperationConfigDetailResponse {

    @NotBlank
    @Size(min = 2, max = 256)
    private String operationName;
    @NotBlank
    @Size(min = 1, max = 256)
    private String templateVersion;
    @NotNull
    @Positive
    private Integer templateId;
    @NotNull
    private boolean mobileTokenEnabled;
    @Size(min = 2, max = 256)
    private String mobileTokenMode;
    @NotNull
    private boolean afsEnabled;
    @Size(min = 2, max = 256)
    private String afsConfigId;
    @Positive
    private Integer expirationTime;

}
