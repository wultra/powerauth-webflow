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
package io.getlime.security.powerauth.lib.nextstep.model.enumeration;

/**
 * Enum representing OTP generation algorithms.
 */
public enum OtpGenerationAlgorithm {

    /**
     * OTP is generated using a digest of request data.
     */
    OTP_DATA_DIGEST,

    /**
     * OTP is generated as digit groups with unique numbers.
     */
    OTP_RANDOM_DIGIT_GROUPS,

}
