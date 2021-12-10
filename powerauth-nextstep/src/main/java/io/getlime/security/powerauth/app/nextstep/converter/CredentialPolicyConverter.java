/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.getlime.security.powerauth.app.nextstep.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialPolicyDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValidationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UsernameGenerationParam;
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
        final CredentialPolicyDetail credentialPolicyDetail = new CredentialPolicyDetail();
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
        credentialPolicyDetail.setTemporaryCredentialExpirationTime(credentialPolicy.getTemporaryCredentialExpirationTime());
        credentialPolicyDetail.setUsernameGenAlgorithm(credentialPolicy.getUsernameGenAlgorithm());
        try {
            credentialPolicyDetail.setUsernameGenParam(parameterConverter.fromString(credentialPolicy.getUsernameGenParam(), UsernameGenerationParam.class));
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        credentialPolicyDetail.setCredentialGenAlgorithm(credentialPolicy.getCredentialGenAlgorithm());
        try {
            credentialPolicyDetail.setCredentialGenParam(parameterConverter.fromString(credentialPolicy.getCredentialGenParam(), CredentialGenerationParam.class));
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        try {
            credentialPolicyDetail.setCredentialValParam(parameterConverter.fromString(credentialPolicy.getCredentialValParam(), CredentialValidationParam.class));
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        credentialPolicyDetail.setTimestampCreated(credentialPolicy.getTimestampCreated());
        credentialPolicyDetail.setTimestampLastUpdated(credentialPolicy.getTimestampLastUpdated());
        return credentialPolicyDetail;
    }

}