/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.mtoken.model.enumeration;

/**
 * This class contains a list of all possible error codes the mobile token API may return.
 * Return some of these messages as error codes in the context of our standard
 * {@link io.getlime.core.rest.model.base.response.ErrorResponse} responses.
 *
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class ErrorCode {

    /**
     * Error code for situation when registration to push notification fails.
     */
    public static final String PUSH_REGISTRATION_FAILED     = "PUSH_REGISTRATION_FAILED";

    /**
     * Error code for situation when an invalid / malformed request is sent.
     */
    public static final String INVALID_REQUEST              = "INVALID_REQUEST";

    /**
     * Error code for situation when an activation is not active.
     */
    public static final String ACTIVATION_NOT_ACTIVE        = "ACTIVATION_NOT_ACTIVE";

    /**
     * Error code for situation when an activation is not configured.
     */
    public static final String ACTIVATION_NOT_CONFIGURED    = "ACTIVATION_NOT_CONFIGURED";

    /**
     * Error code for situation when an invalid activation / device is
     * attempted for operation manipulation.
     */
    public static final String INVALID_ACTIVATION           = "INVALID_ACTIVATION";

    /**
     * Error code for situation when signature verification fails.
     */
    public static final String POWERAUTH_AUTH_FAIL          = "POWERAUTH_AUTH_FAIL";

    /**
     * Error code for situation when an operation was already completed and yet,
     * some further action was requested with that operation.
     */
    public static final String OPERATION_ALREADY_FINISHED   = "OPERATION_ALREADY_FINISHED";

    /**
     * Error code for situation when an operation was failed and yet, some further
     * action was requested with that operation.
     */
    public static final String OPERATION_ALREADY_FAILED     = "OPERATION_ALREADY_FAILED";

    /**
     * Error code for situation when an operation was canceled and yet, some further
     * action other than cancellation was requested with that operation.
     */
    public static final String OPERATION_ALREADY_CANCELED   = "OPERATION_ALREADY_CANCELED";

    /**
     * Error code for situation when an operation expired and yet, some further
     * action was requested with that operation.
     */
    public static final String OPERATION_EXPIRED            = "OPERATION_EXPIRED";

}
