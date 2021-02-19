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
package io.getlime.security.powerauth.app.nextstep.converter;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialPolicyDetail;

/**
 * Converter for credential policies.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialPolicyConverter {

    /**
     * Convert credential policy from entity to detail.
     * @param credentialPolicy Credential policy entity.
     * @return Credential policy detail.
     */
    public CredentialPolicyDetail fromEntity(CredentialPolicyEntity credentialPolicy) {
        CredentialPolicyDetail credentialPolicyDetail = new CredentialPolicyDetail();
        credentialPolicyDetail.setCredentialPolicyName(credentialPolicy.getName());
        credentialPolicyDetail.setDescription(credentialPolicy.getDescription());
        credentialPolicyDetail.setCredentialPolicyStatus(credentialPolicy.getStatus());
        credentialPolicyDetail.setUsernameLengthMin(credentialPolicy.getUsernameLengthMin());
        credentialPolicyDetail.setUsernameLengthMax(credentialPolicy.getUsernameLengthMax());
        credentialPolicyDetail.setUsernameAllowedChars(credentialPolicy.getUsernameAllowedChars());
        credentialPolicyDetail.setCredentialLengthMin(credentialPolicy.getCredentialLengthMin());
        credentialPolicyDetail.setCredentialLengthMax(credentialPolicy.getCredentialLengthMax());
        credentialPolicyDetail.setCredentialAllowedChars(credentialPolicy.getCredentialAllowedChars());
        credentialPolicyDetail.setLimitSoft(credentialPolicy.getLimitSoft());
        credentialPolicyDetail.setLimitHard(credentialPolicy.getLimitHard());
        credentialPolicyDetail.setCheckHistoryCount(credentialPolicy.getCheckHistoryCount());
        credentialPolicyDetail.setRotationEnabled(credentialPolicy.isRotationEnabled());
        credentialPolicyDetail.setRotationDays(credentialPolicy.getRotationDays());
        credentialPolicyDetail.setUsernameGenAlgorithm(credentialPolicy.getUsernameGenAlgorithm());
        credentialPolicyDetail.setCredentialGenAlgorithm(credentialPolicy.getCredentialGenAlgorithm());
        credentialPolicyDetail.setTimestampCreated(credentialPolicy.getTimestampCreated());
        credentialPolicyDetail.setTimestampLastUpdated(credentialPolicy.getTimestampLastUpdated());
        return credentialPolicyDetail;
    }

}