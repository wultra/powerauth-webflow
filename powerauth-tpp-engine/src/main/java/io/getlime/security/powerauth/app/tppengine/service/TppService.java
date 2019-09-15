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

import io.getlime.security.powerauth.app.tppengine.converter.TppAppConverter;
import io.getlime.security.powerauth.app.tppengine.model.response.TppAppDetailResponse;
import io.getlime.security.powerauth.app.tppengine.repository.TppAppDetailRepository;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.TppAppDetailEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service from handling information about TPP and TPP apps.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
public class TppService {

    private final TppAppDetailRepository appDetailRepository;

    @Autowired
    public TppService(TppAppDetailRepository appDetailRepository) {
        this.appDetailRepository = appDetailRepository;
    }

    /**
     * Fetch application details by provided client ID (OAuth 2.0 identification).
     *
     * @param clientId Client ID.
     * @return Application details for app with given client ID, or null
     * if no app for given client ID exists.
     */
    public TppAppDetailResponse fetchAppDetailByClientId(String clientId) {
        final Optional<TppAppDetailEntity> tppAppEntityOptional = appDetailRepository.findByClientId(clientId);
        return tppAppEntityOptional
                .map(TppAppConverter::fromTppAppEntity)
                .orElse(null);
    }
}
