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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.ApplicationEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ApplicationStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Crud repository for persistence of Next Step applications.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface ApplicationRepository extends CrudRepository<ApplicationEntity, Long> {

    /**
     * Find application by name.
     *
     * @param name Application name.
     * @return Application entity.
     */
    Optional<ApplicationEntity> findByName(String name);

    /**
     * Find applications by application status.
     *
     * @param status Application status.
     * @return Application list.
     */
    @Query(value = "from ApplicationEntity a where a.status = :status")
    List<ApplicationEntity> findApplicationsByStatus(@Param("status") ApplicationStatus status);

}