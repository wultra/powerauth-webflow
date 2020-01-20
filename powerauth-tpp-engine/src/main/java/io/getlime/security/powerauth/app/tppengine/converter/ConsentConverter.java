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

package io.getlime.security.powerauth.app.tppengine.converter;

import io.getlime.security.powerauth.app.tppengine.model.response.ConsentDetailResponse;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.ConsentEntity;

/**
 * Converter related to consent entity conversions.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class ConsentConverter {

    /**
     * Convert a new consent detail response from database ce.
     * @param ce Database ce representing a consent.
     * @return Response object for consent detail.
     */
    public ConsentDetailResponse fromConsentEntity(ConsentEntity ce) {
        return new ConsentDetailResponse(ce.getId(), ce.getName(), ce.getText(), ce.getVersion());
    }

}
