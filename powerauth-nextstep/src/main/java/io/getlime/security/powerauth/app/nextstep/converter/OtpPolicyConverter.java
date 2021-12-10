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
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpPolicyEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpPolicyDetail;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;

/**
 * Converter for OTP policies.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OtpPolicyConverter {

    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Convert OTP policy entity to detail.
     * @param otpPolicy OTP policy entity.
     * @return OTP policy detail.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public OtpPolicyDetail fromEntity(OtpPolicyEntity otpPolicy) throws InvalidConfigurationException {
        final OtpPolicyDetail otpPolicyDetail = new OtpPolicyDetail();
        otpPolicyDetail.setOtpPolicyName(otpPolicy.getName());
        otpPolicyDetail.setDescription(otpPolicy.getDescription());
        otpPolicyDetail.setOtpPolicyStatus(otpPolicy.getStatus());
        otpPolicyDetail.setLength(otpPolicy.getLength());
        otpPolicyDetail.setAttemptLimit(otpPolicy.getAttemptLimit());
        otpPolicyDetail.setGenAlgorithm(otpPolicy.getGenAlgorithm());
        try {
            otpPolicyDetail.setGenParam(parameterConverter.fromString(otpPolicy.getGenParam(), OtpGenerationParam.class));
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        otpPolicyDetail.setExpirationTime(otpPolicy.getExpirationTime());
        otpPolicyDetail.setTimestampCreated(otpPolicy.getTimestampCreated());
        otpPolicyDetail.setTimestampLastUpdated(otpPolicy.getTimestampLastUpdated());
        return otpPolicyDetail;
    }

}