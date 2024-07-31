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

package io.getlime.security.powerauth.app.webflow.demo.oauth;

import io.getlime.security.powerauth.app.webflow.demo.model.UserResponse;
import org.springframework.social.ApiBinding;

/**
 * Interface defining the API binding.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public interface DefaultApiBinding extends ApiBinding {

    /**
     * Get user profile.
     *
     * @return User profile.
     */
    UserResponse getProfile();

}