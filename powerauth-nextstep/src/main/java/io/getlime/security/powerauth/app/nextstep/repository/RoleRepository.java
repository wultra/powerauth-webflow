/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.repository;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Crud repository for persistence of roles.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

    /**
     * Find role by name.
     * @param name Role name.
     * @return Role.
     */
    Optional<RoleEntity> findByName(String name);

}