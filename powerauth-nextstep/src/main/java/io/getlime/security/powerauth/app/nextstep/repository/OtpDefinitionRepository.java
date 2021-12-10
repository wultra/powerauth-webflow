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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpDefinitionEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpDefinitionStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Crud repository for persistence of one time password definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface OtpDefinitionRepository extends CrudRepository<OtpDefinitionEntity, Long> {

    /**
     * Find OTP definition by name.
     * @param name OTP definition name.
     * @return OTP definition.
     */
    Optional<OtpDefinitionEntity> findByName(String name);

    /**
     * Find OTP definitions by status.
     * @param status OTP definition status.
     * @return List of OTP definitions.
     */
    @Query(value = "from OtpDefinitionEntity od where od.status = :status")
    List<OtpDefinitionEntity> findOtpDefinitionByStatus(@Param("status") OtpDefinitionStatus status);

}