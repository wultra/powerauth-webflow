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

package io.getlime.security.powerauth.app.tppengine.repository;

import io.getlime.security.powerauth.app.tppengine.repository.model.entity.TppEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for fetching the TPP entities.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Repository
public interface TppRepository extends CrudRepository<TppEntity, Long> {

    /**
     * Find the first TPP entity with provided license number. License number must be unique in the database,
     * two subjects cannot share the same license number.
     *
     * @param tppLicense TPP license identification.
     * @return TPP entity, returned as optional.
     */
    Optional<TppEntity> findFirstByTppLicense(String tppLicense);

}
