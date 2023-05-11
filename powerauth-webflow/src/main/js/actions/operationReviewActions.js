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
 * Get operation detail.
 * @returns {Function} Operation detail.
 */
export function getOperationData() {
    return function (dispatch) {
        axios.post("./api/auth/operation/detail", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash
            }
        }).then((response) => {
            if (response.data.chosenAuthMethod) {
                // if authMethod is already chosen, skip choice and go directly to the authMethod
                switch (response.data.chosenAuthMethod) {
                    case "POWERAUTH_TOKEN":
                        dispatch({
                            type: "SHOW_SCREEN_TOKEN",
                            payload: response.data
                        });
                        return null;
                    case "SMS_KEY":
                        dispatch({
                            type: "SHOW_SCREEN_SMS",
                            payload: response.data
                        });
                        return null;
                    // otherwise show regular operation review with authMethod choice
                }
            }
            dispatch({
                type: "SHOW_SCREEN_OPERATION_REVIEW",
                payload: response.data
            });
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Cancel operation.
 * @returns {Function} No return value.
 */
export function cancel() {
    return function (dispatch) {
        axios.post("./api/auth/operation/cancel", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash
            }
        }).then((response) => {
            dispatch({
                type: "SHOW_SCREEN_ERROR",
                payload: {
                    message: response.data.message
                }
            });
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Update an operation.
 * @param formData Operation form data.
 * @param chosenAuthMethod Chosen authentication method.
 * @param callback Callback to call when call is finished.
 * @returns {Function} No return value.
 */
export function updateOperation(formData, chosenAuthMethod, callback) {
    return function (dispatch) {
        axios.put("./api/auth/operation/formData", {
            formData: formData
        }, {
            headers: {
                'X-OPERATION-HASH': operationHash
            }
        }).then((response) => {
            axios.put("./api/auth/operation/chosenAuthMethod", {
                chosenAuthMethod: chosenAuthMethod
            }, {
                headers: {
                    'X-OPERATION-HASH': operationHash
                }
            });
            callback();
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}
