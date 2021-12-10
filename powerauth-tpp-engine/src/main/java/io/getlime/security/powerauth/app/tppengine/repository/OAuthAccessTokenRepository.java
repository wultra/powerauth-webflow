/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2020 Wultra s.r.o.
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

import io.getlime.security.powerauth.app.tppengine.repository.model.entity.OAuthAccessTokenEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Petr Dvorak, petr@wultra.com
 */
@Repository
public interface OAuthAccessTokenRepository extends CrudRepository<OAuthAccessTokenEntity, String> {

    /**
     * Delete all access tokens by given client ID.
     * @param clientId Client ID of TPP app for which to delete all access tokens.
     */
    void deleteAllByClientId(String clientId);

}
