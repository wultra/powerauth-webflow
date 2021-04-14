/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.repository;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthMethodEntity;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Crud repository for persistence of authentication methods.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface AuthMethodRepository extends CrudRepository<AuthMethodEntity, AuthMethod> {

    /**
     * Find all authentication methods supported by Next Step server.
     *
     * @return List of supported authentication methods.
     */
    @Query("SELECT m FROM AuthMethodEntity m ORDER by m.orderNumber")
    List<AuthMethodEntity> findAllAuthMethods();

    /**
     * Find an authentication method by its name.
     *
     * @param authMethod Name of authentication method.
     * @return Authentication method.
     */
    Optional<AuthMethodEntity> findByAuthMethod(AuthMethod authMethod);
}
