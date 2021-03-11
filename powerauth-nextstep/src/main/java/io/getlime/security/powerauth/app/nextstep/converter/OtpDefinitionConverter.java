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
        OtpDefinitionDetail otpDefinitionDetail = new OtpDefinitionDetail();
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