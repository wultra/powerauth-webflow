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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialPolicyDetail;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;

/**
 * Converter for credential policies.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialPolicyConverter {

    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Convert credential policy from entity to detail.
     * @param credentialPolicy Credential policy entity.
     * @return Credential policy detail.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public CredentialPolicyDetail fromEntity(CredentialPolicyEntity credentialPolicy) throws InvalidConfigurationException {
        CredentialPolicyDetail credentialPolicyDetail = new CredentialPolicyDetail();
        credentialPolicyDetail.setCredentialPolicyName(credentialPolicy.getName());
        credentialPolicyDetail.setDescription(credentialPolicy.getDescription());
        credentialPolicyDetail.setCredentialPolicyStatus(credentialPolicy.getStatus());
        credentialPolicyDetail.setUsernameLengthMin(credentialPolicy.getUsernameLengthMin());
        credentialPolicyDetail.setUsernameLengthMax(credentialPolicy.getUsernameLengthMax());
        credentialPolicyDetail.setUsernameAllowedPattern(credentialPolicy.getUsernameAllowedPattern());
        credentialPolicyDetail.setCredentialLengthMin(credentialPolicy.getCredentialLengthMin());
        credentialPolicyDetail.setCredentialLengthMax(credentialPolicy.getCredentialLengthMax());
        credentialPolicyDetail.setLimitSoft(credentialPolicy.getLimitSoft());
        credentialPolicyDetail.setLimitHard(credentialPolicy.getLimitHard());
        credentialPolicyDetail.setCheckHistoryCount(credentialPolicy.getCheckHistoryCount());
        credentialPolicyDetail.setRotationEnabled(credentialPolicy.isRotationEnabled());
        credentialPolicyDetail.setRotationDays(credentialPolicy.getRotationDays());
        credentialPolicyDetail.setUsernameGenAlgorithm(credentialPolicy.getUsernameGenAlgorithm());
        try {
            credentialPolicyDetail.setUsernameGenParam(parameterConverter.fromString(credentialPolicy.getUsernameGenParam()));
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        credentialPolicyDetail.setCredentialGenAlgorithm(credentialPolicy.getCredentialGenAlgorithm());
        try {
            credentialPolicyDetail.setCredentialGenParam(parameterConverter.fromString(credentialPolicy.getCredentialGenParam()));
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        try {
            credentialPolicyDetail.setCredentialValParam(parameterConverter.fromString(credentialPolicy.getCredentialValParam()));
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        credentialPolicyDetail.setTimestampCreated(credentialPolicy.getTimestampCreated());
        credentialPolicyDetail.setTimestampLastUpdated(credentialPolicy.getTimestampLastUpdated());
        return credentialPolicyDetail;
    }

}