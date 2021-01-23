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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Request object used for creating a new operation.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
public class CreateOperationRequest {

    @NotNull
    private String operationName;
    @NotNull
    private String operationId;
    @NotNull
    private String operationData;
    private String userId;
    private String organizationId;
    private String externalTransactionId;
    private final List<KeyValueParameter> params = new ArrayList<>();
    private OperationFormData formData;
    private ApplicationContext applicationContext;

}
