/*
 * Copyright 2020 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.repository;

import io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity.CertificateVerificationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Crud repository for information about client TLS certificate verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public interface CertificateVerificationRepository extends CrudRepository<CertificateVerificationEntity, CertificateVerificationEntity.CertificateVerificationKey> {

    Optional<CertificateVerificationEntity> findByCertificateVerificationKey(CertificateVerificationEntity.CertificateVerificationKey key);

}
