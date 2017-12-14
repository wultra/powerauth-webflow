/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
                'X-OPERATION-HASH': operationHash,
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
                'X-OPERATION-HASH': operationHash,
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
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            axios.put("./api/auth/operation/chosenAuthMethod", {
                chosenAuthMethod: chosenAuthMethod
            }, {
                headers: {
                    'X-OPERATION-HASH': operationHash,
                }
            });
            callback();
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}
