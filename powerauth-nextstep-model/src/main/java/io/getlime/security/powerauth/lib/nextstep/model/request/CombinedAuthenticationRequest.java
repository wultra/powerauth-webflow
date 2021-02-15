/*
 * Copyright 2021 Wultra s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialAuthenticationMode;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Request object used for authenticating using a credential and an OTP.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CombinedAuthenticationRequest {

    @NotNull
    private String credentialName;
    @NotNull
    private String userId;
    @NotNull
    private String credentialValue;
    private CredentialAuthenticationMode authenticationMode;
    private List<Integer> credentialPositionsToVerify;
    // Either otpId or operationId should be present
    private String otpId;
    private String operationId;
    @NotNull
    private String otpValue;
    // Operation ID is extracted from OTP record in case that otpId is sent
    private boolean updateOperation;

}
