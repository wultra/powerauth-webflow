/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.repository;

import io.getlime.security.powerauth.app.tppengine.repository.model.entity.TppAppDetailEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for fetching details about TPP providers.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Repository
public interface TppAppDetailRepository extends CrudRepository<TppAppDetailEntity, TppAppDetailEntity.TppAppDetailKey> {

    @Query("SELECT app FROM TppAppDetailEntity app WHERE app.primaryKey.appClientId=:clientId")
    Optional<TppAppDetailEntity> findByClientId(@Param("clientId") String clientId);

    @Query("SELECT app FROM TppAppDetailEntity app WHERE app.primaryKey.tppId=:tppId")
    Iterable<TppAppDetailEntity> findByTppId(@Param("tppId") Long tppId);

    @Query("SELECT app FROM TppAppDetailEntity app WHERE app.primaryKey.tppId=:tppId AND app.appName=:appName")
    Iterable<TppAppDetailEntity> findByTppIdAndAppName(@Param("tppId") Long tppId, @Param("appName") String appName);


}
