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

import {dispatchAction} from "../dispatcher/dispatcher";

/**
 * Shared logic for handling authentication failed errors. In case the error is critical, dispatch error immediately.
 *
 * @param dispatch Dispatch.
 * @param response Response.
 * @returns {boolean} True in case error has been handled.
 */
export function handleAuthFailedError(dispatch, response) {
    // handle timeout - login action can not succeed anymore, do not show login screen, show error instead
    if (response.data.message === "operation.timeout") {
        dispatchAction(dispatch, response);
        return true;
    }
    // if the operation has been interrupted by new operation, show an error
    if (response.data.message === "operation.interrupted") {
        dispatchAction(dispatch, response);
        return true;
    }
    // if the operation is no longer available, show an error
    if (response.data.message === "operation.notAvailable") {
        dispatchAction(dispatch, response);
        return true;
    }
    // if the maximum number of attempts has been exceeded, show an error, the method cannot continue
    if (response.data.message === "authentication.maxAttemptsExceeded") {
        dispatchAction(dispatch, response);
        return true;
    }
    // if there is no supported auth method, show error, there is no point in continuing
    // TODO - handle fallback - see issue #32
    if (response.data.message === "error.noAuthMethod") {
        dispatchAction(dispatch, response);
        return true;
    }
    return false;
}