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
                type: "SHOW_SCREEN_SMS",
                payload: response.data
            });
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Initialize SMS authentication.
 * @returns {Function} No return value.
 */
export function init() {
    return function (dispatch) {
        axios.post("./api/auth/sms/init", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            if (response.data.result === 'AUTH_FAILED') {
                dispatchAction(dispatch, response);
                return;
            }
            dispatch({
                type: "SHOW_SCREEN_SMS",
                payload: {
                    loading: false,
                    error: false,
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
 * Perform SMS authentication.
 * @param userAuthCode User supplied code.
 * @returns {Function} No return value.
 */
export function authenticate(userAuthCode) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_SMS",
            payload: {
                loading: true,
                error: false,
                message: ""
            }
        });
        axios.post("./api/auth/sms/authenticate", {
            authCode: userAuthCode
        }, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
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
                    // if the maximum number of SMS OTP messages has been exceeded, show an error, the method cannot continue
                    if (response.data.message === "smsAuthorization.maxAttemptsExceeded") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    // if there is no supported auth method, show error, there is no point in continuing
                    if (response.data.message === "error.noAuthMethod") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    dispatch({
                        type: "SHOW_SCREEN_SMS",
                        payload: {
                            loading: false,
                            error: true,
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
 * Cancel operation.
 * @returns {Function} No return value.
 */
export function cancel() {
    return function (dispatch) {
        axios.post("./api/auth/sms/cancel", {}, {
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
