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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Request object used for looking up a user identity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class LookupUserRequest {

    // Filter by user identity status
    private UserIdentityStatus status;

    // Filter by created date
    private Date createdStartDate;
    private Date createdEndDate;

    // Filter by roles
    private List<String> roles = new ArrayList<>();

    // Filter by username and credentialName to allow username -> user ID mapping
    private String username;
    private String credentialName;

}
