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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpDefinitionEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpDefinitionDetail;

/**
 * Converter for OTP definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OtpDefinitionConverter {

    /**
     * Convert OTP definition entity to detail.
     * @param otpDefinition OTP definition entity.
     * @return OTP definition detail.
     */
    public OtpDefinitionDetail fromEntity(OtpDefinitionEntity otpDefinition) {
        final OtpDefinitionDetail otpDefinitionDetail = new OtpDefinitionDetail();
        otpDefinitionDetail.setOtpDefinitionName(otpDefinition.getName());
        otpDefinitionDetail.setDescription(otpDefinition.getDescription());
        otpDefinitionDetail.setOtpDefinitionStatus(otpDefinition.getStatus());
        otpDefinitionDetail.setApplicationName(otpDefinition.getApplication().getName());
        otpDefinitionDetail.setOtpPolicyName(otpDefinition.getOtpPolicy().getName());
        otpDefinitionDetail.setEncryptionEnabled(otpDefinition.isEncryptionEnabled());
        otpDefinitionDetail.setEncryptionAlgorithm(otpDefinition.getEncryptionAlgorithm());
        otpDefinitionDetail.setDataAdapterProxyEnabled(otpDefinition.isDataAdapterProxyEnabled());
        otpDefinitionDetail.setTimestampCreated(otpDefinition.getTimestampCreated());
        otpDefinitionDetail.setTimestampLastUpdated(otpDefinition.getTimestampLastUpdated());
        return otpDefinitionDetail;
    }

}