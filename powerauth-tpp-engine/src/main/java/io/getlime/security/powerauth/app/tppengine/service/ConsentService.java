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
