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
package io.getlime.security.powerauth.lib.nextstep.model.response;

import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserContactDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Response object used for getting a user identity detail.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class GetUserDetailResponse {

    @NotNull
    private String userId;

    @NotNull
    private UserIdentityStatus userIdentityStatus;

    @NotNull
    private Map<String, Object> extras = new LinkedHashMap<>();

    @NotNull
    private List<String> roles = new ArrayList<>();

    @NotNull
    private List<UserContactDetail> contacts = new ArrayList<>();

    @NotNull
    private List<CredentialDetail> credentials = new ArrayList<>();

    @NotNull
    private Date timestampCreated;

    @NotNull
    private Date timestampLastUpdated;

}
