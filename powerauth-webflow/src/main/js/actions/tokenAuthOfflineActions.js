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
                    dataHash: response.data.dataHash,
                    chosenActivation: response.data.chosenActivation,
                    activations: response.data.activations
                }
            });
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

export function changeActivation(activation) {
    return function (dispatch) {
        dispatch({
            type: "CHANGE_ACTIVATION",
            payload: {
                chosenActivation: activation,
                init: false
            }
        });
    }
}

export function authenticateOffline(activationId, authCode, nonce, dataHash) {
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
            activationId: activationId,
            authCode: authCode,
            nonce: nonce,
            dataHash: dataHash
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
                            message: response.data.message
                        }
                    });
                    break;
                }
            }
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

