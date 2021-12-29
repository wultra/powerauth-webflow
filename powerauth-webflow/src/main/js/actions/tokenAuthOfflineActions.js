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
import {dispatchAction, dispatchError} from "../dispatcher/dispatcher";
import {handleAuthFailedError} from "./errorHandling";

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
                    qrCode: response.data.qrCode,
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
                case 'CANCELED':
                case 'AUTH_METHOD_FAILED': {
                    dispatch({
                        type: "SHOW_SCREEN_ERROR",
                        payload: {
                            message: response.data.message
                        }
                    });
                    break;
                }
                case 'AUTH_FAILED': {
                    if (!handleAuthFailedError(dispatch, response)) {
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
                    }
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
 * @param callback Callback function to execute.
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

