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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object used for creating a new operation.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
public class CreateOperationRequest {

    @NotBlank
    @Size(min = 2, max = 256)
    private String operationName;
    @Size(min = 1, max = 256)
    private String operationId;
    @NotNull
    @Size(max = 256)
    private String operationData;
    @Size(min = 2, max = 256)
    private String operationNameExternal;
    @Size(min = 1, max = 256)
    private String userId;
    @Size(min = 2, max = 256)
    private String organizationId;
    @Size(min = 1, max = 256)
    private String externalTransactionId;

    @JsonSetter(nulls = Nulls.SKIP)
    private final List<KeyValueParameter> params = new ArrayList<>();
    private OperationFormData formData;
    private ApplicationContext applicationContext;

}
