/*
 * Copyright 2017 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.lib.nextstep.model.response;

import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    @NotBlank
    @Size(min = 1, max = 256)
    private String operationData;
    @NotNull
    private final List<AuthStep> steps = new ArrayList<>();
    private OperationFormData formData;

}
