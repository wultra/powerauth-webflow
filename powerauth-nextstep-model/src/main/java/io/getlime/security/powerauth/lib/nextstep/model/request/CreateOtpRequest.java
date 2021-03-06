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

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Request object used for creating an OTP.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CreateOtpRequest {

    // The userId is optional to allow creating OTP codes for users without identity
    @Size(min = 1, max = 256)
    private String userId;
    @NotBlank
    @Size(min = 2, max = 256)
    private String otpName;
    // Optional credential name for updating credential counters
    @Size(min = 2, max = 256)
    private String credentialName;
    // The otpData parameter has priority over data extracted from operation.
    // The otpData can be an empty string, null value indicates data taken from operation.
    @Size(max = 256)
    private String otpData;
    @Size(min = 1, max = 256)
    private String operationId;

}
