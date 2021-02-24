/*
 * Copyright 2012 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpPolicyEntity;
import io.getlime.security.powerauth.crypto.lib.model.exception.GenericCryptoException;
import io.getlime.security.powerauth.crypto.server.util.DataDigest;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpValueDetail;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpGenAlgorithmNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * This service handles persistence of one time passwords.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OtpGenerationService {

    private final Logger logger = LoggerFactory.getLogger(OtpGenerationService.class);

    /**
     * Generate an OTP value using algorithm specified in OTP policy.
     * @param otpData OTP data to use.
     * @param otpPolicy OTP policy.
     * @return Generated OTP value.
     */
    public OtpValueDetail generateOtpValue(String otpData, OtpPolicyEntity otpPolicy) throws OtpGenAlgorithmNotSupportedException, InvalidConfigurationException {
        OtpValueDetail otpValueDetail = new OtpValueDetail();
        Integer length = otpPolicy.getLength();
        String otpGenAlgorithm = otpPolicy.getGenAlgorithm();
        switch (otpGenAlgorithm) {
            case "DEFAULT":
            case "OTP_DATA_DIGEST":
                try {
                    DataDigest dataDigest = new DataDigest(length);
                    DataDigest.Result result = dataDigest.generateDigest(Collections.singletonList(otpData));
                    otpValueDetail.setSalt(result.getSalt());
                    otpValueDetail.setOtpValue(result.getDigest());
                } catch (GenericCryptoException ex) {
                    throw new InvalidConfigurationException("OTP generation failed, error: " + ex.getMessage());
                }
                break;

            case "OTP_RANDOM_DIGIT_PAIRS":
            default:
                throw new OtpGenAlgorithmNotSupportedException("OTP generation algorithm is not supported: " + otpGenAlgorithm);
        }
        return otpValueDetail;
    }
}