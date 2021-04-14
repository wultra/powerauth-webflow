/*
 * Copyright 2019 Wultra s.r.o.
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
