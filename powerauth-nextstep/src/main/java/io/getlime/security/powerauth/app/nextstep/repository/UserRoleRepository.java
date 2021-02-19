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
package io.getlime.security.powerauth.app.nextstep.repository;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.RoleEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserRoleEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserRoleStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Crud repository for persistence of user roles.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface UserRoleRepository extends CrudRepository<UserRoleEntity, Long> {

    /**
     * Find roles for a user identity.
     * @param userId User identity entity.
     * @return List of user roles.
     */
    List<UserRoleEntity> findAllByUserId(UserIdentityEntity userId);

    /**
     * Find user roles by a role.
     * @param role Role
     * @return List of user roles.
     */
    List<UserRoleEntity> findAllByRole(RoleEntity role);

    /**
     * Find roles for a user identity with given status.
     * @param userId User identity entity.
     * @param status User role status.
     * @return List of user roles.
     */
    List<UserRoleEntity> findAllByUserIdAndStatus(UserIdentityEntity userId, UserRoleStatus status);

    /**
     * Find role for a user identity.
     * @param userId User identity entity.
     * @param role Role.
     * @return User role.
     */
    Optional<UserRoleEntity> findByUserIdAndRole(UserIdentityEntity userId, RoleEntity role);

}