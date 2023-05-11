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
import axios from 'axios';
import {dispatchAction, dispatchError} from '../dispatcher/dispatcher'
import {handleAuthFailedError} from "./errorHandling";

/**
 * Initiate authentication/authorization process.
 * @returns {Function} No return value.
 */
export function authenticate(callback) {
    return function (dispatch) {
        axios.post("./api/auth/init/authenticate", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash
            }
        }).then((response) => {
            // Save operation hash in case the operation has been just initialized (default operation)
            if (operationHash === null) {
                operationHash = response.data.operationHash;
            }
            if (!handleAuthFailedError(dispatch, response)) {
                // Callback is used to initialize Web Socket connection after handshake has been completed
                callback();
                dispatchAction(dispatch, response);
            }
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}
