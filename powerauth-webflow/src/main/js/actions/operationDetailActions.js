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

import axios from "axios";
import {dispatchError} from "../dispatcher/dispatcher";

/**
 * Update operation form data on the server.
 * @param formData Operation form data.
 * @returns {Function} No response in case of OK status, otherwise error is dispatched.
 */
export function updateFormData(formData) {
    return function (dispatch) {
        axios.put("./api/auth/operation/formData", {
            formData: formData
        }, {
            headers: {
                'X-OPERATION-HASH': operationHash
            }
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Interrupt operation and show unexpected error about missing bank accounts.
 * @returns {Function} Missing bank accounts error is dispatched.
 */
export function missingBankAccountsError() {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_ERROR",
            payload: {
                message: "operationReview.bankAccountsMissing"
            }
        })
    }
}
