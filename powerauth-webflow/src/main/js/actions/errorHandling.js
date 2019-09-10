/*
 * Copyright 2019 Wultra s.r.o.
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