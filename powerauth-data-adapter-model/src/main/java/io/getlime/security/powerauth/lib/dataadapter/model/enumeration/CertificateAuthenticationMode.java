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
package io.getlime.security.powerauth.lib.dataadapter.model.enumeration;

/**
 * Certificate authentication modes:
 * - ENABLED = certificate can be used and UI shows the option
 * - DISABLED = certificate could be normally used, but the UI option is disabled for this case
 * - NOT_AVAILABLE = certificate authentication is not available
 */
public enum CertificateAuthenticationMode {

    /**
     * Certificate authentication is enabled.
     */
    ENABLED,

    /**
     * Certificate authentication is disabled.
     */
    DISABLED,

    /**
     * Certificate authentication is not available.
     */
    NOT_AVAILABLE
}
