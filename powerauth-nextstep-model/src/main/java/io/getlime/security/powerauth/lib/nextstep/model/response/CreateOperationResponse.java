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

import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Response object used for creating a new operation.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
public class CreateOperationResponse {

    @NotBlank
    @Size(min = 1, max = 256)
    private String operationId;
    @NotBlank
    @Size(min = 2, max = 256)
    private String operationName;
    @Size(min = 2, max = 256)
    private String organizationId;
    @Size(min = 2, max = 256)
    private String operationNameExternal;
    @Size(min = 1, max = 256)
    private String externalTransactionId;
    @NotNull
    private AuthResult result;
    private String resultDescription;
    @NotNull
    private Date timestampCreated;
    @NotNull
    private Date timestampExpires;
    @NotNull
    @Size(max = 256)
    private String operationData;
    @NotNull
    private final List<AuthStep> steps = new ArrayList<>();
    private OperationFormData formData;

}
