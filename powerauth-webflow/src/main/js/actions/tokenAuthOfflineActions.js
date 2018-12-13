/*
 * Copyright 2017 Wultra s.r.o.
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
 * Initialize offline mode for mobile token.
 * @param activationId Chosen activation ID.
 * @returns {Function} No return value.
 */
export function initOffline(activationId) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_TOKEN",
            payload: {
                loading: true,
                error: false,
                init: false,
                message: ""
            }
        });
        axios.post("./api/auth/token/offline/init", {
            activationId: activationId
        }, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            if (response.data.result === 'AUTH_FAILED') {
                dispatchAction(dispatch, response);
                return;
            }
            dispatch({
                type: "SHOW_SCREEN_TOKEN",
                payload: {
                    loading: false,
                    error: false,
                    init: true,
                    message: "",
                    qrCode: response.data.qrcode,
                    nonce: response.data.nonce,
                    chosenActivation: response.data.chosenActivation,
                    activations: response.data.activations
                }
            });
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Authenticate in offline mode for mobile token.
 * @param activationId Chosen activation ID.
 * @param authCode User supplied code.
 * @param nonce Nonce.
 * @returns {Function} No return value.
 */
export function authenticateOffline(activationId, authCode, nonce) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_TOKEN",
            payload: {
                loading: true,
                error: false,
                init: false,
                message: ""
            }
        });
        axios.post("./api/auth/token/offline/authenticate", {
            operationHash: operationHash,
            activationId: activationId,
            authCode: authCode,
            nonce: nonce,
        }, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            dispatch({
                type: "SHOW_SCREEN_TOKEN",
                payload: {
                    loading: true,
                    error: false,
                    init: false,
                    message: ""
                }
            });
            switch (response.data.result) {
                case 'CONFIRMED': {
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
                    // handle timeout - action can not succeed anymore, show error
                    if (response.data.message === "operation.timeout") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    // if the operation has been interrupted by new operation, show an error
                    if (response.data.message === "operation.interrupted") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    // if the maximum number of attempts has been exceeded, show an error, the method cannot continue
                    if (response.data.message === "authentication.maxAttemptsExceeded") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    // if there is no supported auth method, show error, there is no point in continuing
                    // TODO - handle fallback - see issue #32
                    if (response.data.message === "error.noAuthMethod") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    dispatch({
                        type: "SHOW_SCREEN_TOKEN",
                        payload: {
                            loading: false,
                            error: true,
                            init: false,
                            message: response.data.message,
                            remainingAttempts: response.data.remainingAttempts
                        }
                    });
                    break;
                }
            }
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Update operation form data on the server.
 * @param formData Operation form data.
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

