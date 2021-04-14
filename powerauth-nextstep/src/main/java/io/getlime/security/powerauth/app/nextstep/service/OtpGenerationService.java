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
package io.getlime.security.powerauth.app.nextstep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpPolicyEntity;
import io.getlime.security.powerauth.crypto.lib.model.exception.GenericCryptoException;
import io.getlime.security.powerauth.crypto.server.util.DataDigest;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpValueDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OtpGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpGenAlgorithmNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This service handles persistence of one time passwords.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OtpGenerationService {

    private final Logger logger = LoggerFactory.getLogger(OtpGenerationService.class);

    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Generate an OTP value using algorithm specified in OTP policy.
     * @param otpData OTP data to use.
     * @param otpPolicy OTP policy.
     * @return Generated OTP value.
     */
    public OtpValueDetail generateOtpValue(String otpData, OtpPolicyEntity otpPolicy) throws OtpGenAlgorithmNotSupportedException, InvalidConfigurationException {
        final OtpValueDetail otpValueDetail = new OtpValueDetail();
        final Integer length = otpPolicy.getLength();
        final OtpGenerationAlgorithm otpGenAlgorithm = otpPolicy.getGenAlgorithm();
        switch (otpGenAlgorithm) {
            case OTP_DATA_DIGEST:
                try {
                    final DataDigest dataDigest = new DataDigest(length);
                    final DataDigest.Result result = dataDigest.generateDigest(Collections.singletonList(otpData));
                    otpValueDetail.setSalt(result.getSalt());
                    otpValueDetail.setOtpValue(result.getDigest());
                } catch (GenericCryptoException ex) {
                    throw new InvalidConfigurationException("OTP generation failed, error: " + ex.getMessage());
                }
                return otpValueDetail;

            case OTP_RANDOM_DIGIT_GROUPS:
                final OtpGenerationParam otpGenerationParam;
                try {
                    otpGenerationParam = parameterConverter.fromString(otpPolicy.getGenParam(), OtpGenerationParam.class);
                } catch (JsonProcessingException ex) {
                    throw new InvalidConfigurationException(ex);
                }
                final Integer groupSize = otpGenerationParam.getGroupSize();
                if (groupSize == null) {
                    throw new InvalidConfigurationException("Invalid configuration of algorithm OTP_RANDOM_DIGIT_GROUPS, group size is not specified");
                }
                if (length % groupSize != 0) {
                    throw new InvalidConfigurationException("Invalid configuration of algorithm OTP_RANDOM_DIGIT_GROUPS, group size does not divide OTP length without remainder");
                }
                final int groupCount = length / groupSize;
                final SecureRandom secureRandomSeed = new SecureRandom();
                // Generate random seed
                final byte[] seed = secureRandomSeed.generateSeed(16);
                final SecureRandom secureRandom = new SecureRandom(seed);
                // Store used seed as salt
                otpValueDetail.setSalt(seed);
                final int groupLimit = (int) Math.pow(10, groupSize);
                final Set<String> groups = new LinkedHashSet<>();
                while (groups.size() < groupCount) {
                    final int randomInt = secureRandom.nextInt(groupLimit);
                    final StringBuilder sb = new StringBuilder();
                    sb.append(randomInt);
                    while (sb.length() < groupSize) {
                        sb.insert(0, "0");
                    }
                    groups.add(sb.toString());
                }
                final StringBuilder otpBuilder = new StringBuilder();
                for (String g : groups) {
                    otpBuilder.append(g);
                }
                otpValueDetail.setOtpValue(otpBuilder.toString());
                return otpValueDetail;

            default:
                throw new OtpGenAlgorithmNotSupportedException("OTP generation algorithm is not supported: " + otpGenAlgorithm);
        }
    }
}