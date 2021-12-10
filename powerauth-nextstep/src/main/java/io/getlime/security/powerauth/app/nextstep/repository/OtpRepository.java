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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Crud repository for persistence of one time passwords.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface OtpRepository extends CrudRepository<OtpEntity, String> {

    /**
     * Remove all OTP entities for user identity.
     * @param userId User identity entity.
     * @return Count of removed OTP entities.
     */
    @Modifying
    @Query("UPDATE OtpEntity o SET o.status = io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpStatus.REMOVED " +
            "WHERE o.userId = ?1 " +
            "AND o.status <> io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpStatus.REMOVED")
    int removeOtpsForUserId(String userId);

    /**
     * Find OTP entities by operation.
     * @param operationEntity Operation entity.
     * @return List of OTP entities.
     */
    Stream<OtpEntity> findAllByOperationOrderByTimestampCreatedDesc(OperationEntity operationEntity);

    /**
     * Find OTP entity by operation.
     * @param operationEntity Operation entity.
     * @return Optional OTP entity.
     */
    Optional<OtpEntity> findFirstByOperationOrderByTimestampCreatedDesc(OperationEntity operationEntity);

    /**
     * Find OTP entities by operation and status.
     * @param operationEntity Operation entity.
     * @param status OTP status.
     * @return List of OTP entities.
     */
    Stream<OtpEntity> findAllByOperationAndStatus(OperationEntity operationEntity, OtpStatus status);

}