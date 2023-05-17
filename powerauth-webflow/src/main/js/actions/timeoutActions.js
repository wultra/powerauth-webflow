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
import axios from "axios";
import {dispatchError} from "../dispatcher/dispatcher";

/**
 * Verify operation timeout and update current timeout values.
 * @returns {Function} No return value.
 */
export function verifyOperationTimeout() {
    return function (dispatch) {
        axios.post("./api/auth/timeout/verify", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash
            }
        }).then((response) => {
            dispatch({
                type: "UPDATE_TIMEOUT",
                payload: {
                    timeoutWarningDelayMs: response.data.timeoutWarningDelayMs,
                    timeoutDelayMs: response.data.timeoutDelayMs,
                    timeoutCheckEnabled: true
                }
            });
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}
