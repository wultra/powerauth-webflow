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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Request object used for creating an anti-fraud system action in Next Step.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CreateAfsActionRequest {

    @NotBlank
    @Size(min = 1, max = 256)
    private String operationId;
    @NotBlank
    @Size(min = 2, max = 256)
    private String afsAction;
    @NotNull
    @Positive
    private Integer stepIndex;
    @Size(min = 2, max = 256)
    private String requestAfsExtras;
    private boolean afsResponseApplied;
    @Size(min = 2, max = 256)
    private String afsLabel;
    @Size(min = 2, max = 256)
    private String responseAfsExtras;
    @NotNull
    private Date timestampCreated;

}
