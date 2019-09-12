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

package io.getlime.security.powerauth.app.tppengine.service;

import io.getlime.security.powerauth.app.tppengine.converter.ConsentConverter;
import io.getlime.security.powerauth.app.tppengine.model.response.ConsentDetailResponse;
import io.getlime.security.powerauth.app.tppengine.repository.ConsentRepository;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.ConsentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service used for accessing consent business logic.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
public class ConsentService {

    private final ConsentRepository consentRepository;

    private final ConsentConverter consentConverter = new ConsentConverter();

    @Autowired
    public ConsentService(ConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    public ConsentDetailResponse consentDetail(String id) {
        final Optional<ConsentEntity> consentEntity = consentRepository.findFirstById(id);
        return consentEntity
                .map(consentConverter::fromConsentEntity)
                .orElse(null);
    }

}
