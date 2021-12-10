/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserPrefsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Crud repository for persistence of user preferences.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface UserPrefsRepository extends CrudRepository<UserPrefsEntity, String> {

    /**
     * Get user preferences for given user. Null is returned in case preferences are not available.
     *
     * @param userId User ID
     * @return User preferences.
     */
    @Query("SELECT p FROM UserPrefsEntity p WHERE p.userId=?1")
    UserPrefsEntity findUserPrefs(String userId);

}
