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
package io.getlime.security.powerauth.lib.webflow.authentication.model;

/**
 * Constants for storing attributes in HTTP session by individual steps.
 */
public class HttpSessionAttributeNames {

    public static final String OTP_ID = "OTP_ID";
    public static final String LAST_MESSAGE_TIMESTAMP = "LAST_MESSAGE_TIMESTAMP";
    public static final String INITIAL_MESSAGE_SENT = "INITIAL_MESSAGE_SENT";
    public static final String AUTH_STEP_OPTIONS = "AUTH_STEP_OPTIONS";
    public static final String PENDING_AUTH_OBJECT = "PENDING_AUTH_OBJECT";
    public static final String CONSENT_SKIPPED = "CONSENT_SKIPPED";
    public static final String USERNAME = "USERNAME";
    public static final String CLIENT_CERTIFICATE = "CLIENT_CERTIFICATE";
    public static final String APPROVAL_BY_CERTIFICATE_ENABLED = "APPROVAL_BY_CERTIFICATE_ENABLED";
    public static final String OPERATION_DATA_EXTERNAL = "OPERATION_DATA_EXTERNAL";

}
