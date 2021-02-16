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

import io.getlime.security.powerauth.lib.nextstep.model.entity.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Response object used for getting the operation detail.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
public class GetOperationDetailResponse {

    @NotNull
    private String operationId;
    @NotNull
    private String operationName;
    private String userId;
    private String organizationId;
    private UserAccountStatus accountStatus;
    private String operationNameExternal;
    private String externalTransactionId;
    @NotNull
    private AuthResult result;
    @NotNull
    private Date timestampCreated;
    @NotNull
    private Date timestampExpires;
    private String operationData;
    @NotNull
    private final List<AuthStep> steps = new ArrayList<>();
    @NotNull
    private final List<OperationHistory> history = new ArrayList<>();
    @NotNull
    private final List<AfsActionDetail> afsActions = new ArrayList<>();
    private OperationFormData formData;
    private AuthMethod chosenAuthMethod;
    private Integer remainingAttempts;
    private ApplicationContext applicationContext;

    public boolean isExpired() {
        return new Date().after(timestampExpires);
    }
}
