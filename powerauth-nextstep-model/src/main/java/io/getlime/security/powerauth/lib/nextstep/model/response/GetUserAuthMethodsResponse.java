/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAuthMethodDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object used for getting the list of supported authentication methods per user.
 *
 * @author Roman Strobl
 */
public class GetUserAuthMethodsResponse {

    private List<UserAuthMethodDetail> userAuthMethods;

    /**
     * Default constructor.
     */
    public GetUserAuthMethodsResponse() {
        userAuthMethods = new ArrayList<>();
    }

    public List<UserAuthMethodDetail> getUserAuthMethods() {
        return userAuthMethods;
    }

    public void setUserAuthMethods(List<UserAuthMethodDetail> userAuthMethods) {
        this.userAuthMethods = userAuthMethods;
    }
}
