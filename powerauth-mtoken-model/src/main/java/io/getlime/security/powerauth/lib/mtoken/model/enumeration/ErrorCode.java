/*
 * Copyright 2018 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.lib.mtoken.model.enumeration;

/**
 * This class contains a list of all possible error codes the mobile token API may return.
 * Return some of these messages as error codes in the context of our standard
 * {@link io.getlime.core.rest.model.base.response.ErrorResponse} responses.
 *
 *
 * @author Petr Dvorak, petr@lime-company.eu
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
