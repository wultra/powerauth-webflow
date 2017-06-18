/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CrudRepository for persistence of authentication methods.
 *
 * @author Roman Strobl
 */
@Component
public interface AuthMethodRepository extends CrudRepository<AuthMethodEntity, AuthMethod> {

    /**
     * Find all authentication methods supported by Next Step server.
     *
     * @return List of supported authentication methods.
     */
    List<AuthMethodEntity> findAllAuthMethods();
}
