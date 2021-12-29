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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.HashConfigEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashConfigStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Crud repository for persistence of hashing configurations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface HashConfigRepository extends CrudRepository<HashConfigEntity, Long> {

    /**
     * Find hashing configuration by name.
     * @param name Hashing configuration name.
     * @return Hashing configuration.
     */
    Optional<HashConfigEntity> findByName(String name);

    /**
     * Find hashing configurations by status.
     * @param status Hashing configuration status.
     * @return List of hashing configurations.
     */
    @Query(value = "from HashConfigEntity cp where cp.status = :status")
    List<HashConfigEntity> findHashConfigByStatus(@Param("status") HashConfigStatus status);

}