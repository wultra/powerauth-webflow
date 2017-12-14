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
import {dispatchAction, dispatchError} from "../dispatcher/dispatcher";

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
            dispatch({
                type: "SHOW_SCREEN_TOKEN",
                payload: response.data
            });
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Initialize online mode for mobile token.
 * @param callback Callback to call when function finishes.
 * @returns {Function} No return value.
 */
export function initOnline(callback) {
    return function (dispatch) {
        axios.post("./api/auth/token/web/init", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            // silently ignore push message failures, see #125
            if (response.data.result === 'AUTH_FAILED' && response.data.message !== 'pushMessage.fail') {
                dispatchAction(dispatch, response);
                return;
            }
            dispatch({
                type: "SHOW_SCREEN_TOKEN",
                payload: response.data
            });
            // initialization complete - mobile token authorization can start
            callback(true);
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Authenticate in online mode for mobile token.
 * @param callback Callback to call when function finishes.
 * @returns {Function} No return value.
 */
export function authenticateOnline(callback) {
    return function (dispatch) {
        axios.post("./api/auth/token/web/authenticate", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            switch (response.data.result) {
                case 'CONFIRMED': {
                    callback(false);
                    dispatchAction(dispatch, response);
                    break;
                }
                case 'CANCELED': {
                    dispatch({
                        type: "SHOW_SCREEN_ERROR",
                        payload: {
                            message: response.data.message
                        }
                    });
                    break;
                }
                case 'AUTH_FAILED': {
                    // handle case when authentication method is no longer available
                    if (response.data.message === "operation.methodNotAvailable") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    // handle timeout - action can not succeed anymore, show error
                    if (response.data.message === "operation.timeout") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    // if there is no supported auth method, show error, there is no point in continuing
                    // TODO - handle fallback - see issue #32
                    if (response.data.message === "error.noAuthMethod") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    callback(true);
                    dispatch({
                        type: "SHOW_SCREEN_TOKEN",
                        payload: {
                            info: "reload"
                        }
                    });
                    break;
                }
            }
            return null;
        }).catch((error) => {
            callback(false);
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
        axios.post("./api/auth/token/web/cancel", {}, {
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
 * Update operation form data on the server.
 * @param formData Operation form data.
 * @param callback Callback to call when function finishes.
 * @returns {Function} No response in case of OK status, otherwise error is dispatched.
 */
export function updateFormData(formData, callback) {
    return function (dispatch) {
        axios.put("./api/auth/operation/formData", {
            formData: formData
        }, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            callback();
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}
