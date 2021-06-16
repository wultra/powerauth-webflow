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
import {handleAuthFailedError} from "./errorHandling";

/**
 * Get operation detail.
 * @param component Component requesting the action.
 * @returns {Function} Operation detail.
 */
export function getOperationData(component) {
    return function (dispatch) {
        axios.post("./api/auth/operation/detail", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            dispatch({
                type: getActionType(component),
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
 * @param component Component requesting the action.
 * @returns {Function} No return value.
 */
export function init(component) {
    return function (dispatch) {
        axios.post("./api/auth/sms/init", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            if (response.data.result === 'AUTH_FAILED') {
                // Handle  error when message delivery fails, another SMS message can be sent later.
                dispatch({
                    type: getActionType(component),
                    payload: {
                        loading: false,
                        error: true,
                        init: true,
                        message: response.data.message,
                        passwordEnabled: response.data.passwordEnabled,
                        smsOtpEnabled: response.data.smsOtpEnabled,
                        username: response.data.username,
                        resendDelay: response.data.resendDelay
                    }
                });
                return null;
            }
            dispatch({
                type: getActionType(component),
                payload: {
                    loading: false,
                    error: false,
                    init: true,
                    message: response.data.message,
                    passwordEnabled: response.data.passwordEnabled,
                    smsOtpEnabled: response.data.smsOtpEnabled,
                    username: response.data.username,
                    resendDelay: response.data.resendDelay
                }
            });
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Resend authorization SMS.
 * @param component Component requesting the action.
 * @returns {Function} No return value.
 */
export function resend(component) {
    return function (dispatch) {
        axios.post("./api/auth/sms/resend", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            if (response.data.result === 'AUTH_FAILED') {
                // Handle error when message delivery fails, another SMS message can be sent later.
                dispatch({
                    type: getActionType(component),
                    payload: {
                        loading: false,
                        error: true,
                        init: false,
                        resend: true,
                        message: response.data.message,
                        resendDelay: response.data.resendDelay
                    }
                });
                return null;
            }
            dispatch({
                type: getActionType(component),
                payload: {
                    loading: false,
                    error: false,
                    init: false,
                    resend: true,
                    message: response.data.message,
                    resendDelay: response.data.resendDelay
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
 * @param userPassword User supplied password.
 * @param component Component requesting the action.
 * @returns {Function} No return value.
 */
export function authenticate(userAuthCode, userPassword, component) {
    return function (dispatch) {
        dispatch({
            type: getActionType(component),
            payload: {
                loading: true,
                error: false,
                init: false,
                message: ""
            }
        });
        axios.post("./api/auth/sms/authenticate", {
            authCode: userAuthCode,
            password: userPassword
        }, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            switch (response.data.result) {
                case 'CONFIRMED': {
                    // Make sure to complete token authentication in case it is still enabled - send push message
                    // to mobile app about completed authentication step.
                    if (component === "TOKEN") {
                        axios.post("./api/auth/token/web/authenticate", {}, {
                            headers: {
                                'X-OPERATION-HASH': operationHash,
                            }
                        }).then((response) => {
                            dispatchAction(dispatch, response);
                            return null;
                        }).catch((error) => {
                            dispatchError(dispatch, error);
                        });
                        break;
                    } else {
                        // Otherwise directly continue with next step
                        dispatchAction(dispatch, response);
                        break;
                    }
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
                            type: getActionType(component),
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
 * Cancel operation.
 * @returns {Function} No return value.
 */
export function cancel(component) {
    return function (dispatch) {
        axios.post("./api/auth/sms/cancel", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            // Make sure to cancel token authentication in case it is still enabled - send push message
            // to mobile app about canceled authentication step.
            if (component === "TOKEN") {
                axios.post("./api/auth/token/web/authenticate", {}, {
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
                });
            } else {
                // Otherwise directly continue with error
                dispatch({
                    type: "SHOW_SCREEN_ERROR",
                    payload: {
                        message: response.data.message
                    }
                });
            }
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Get action type to dispatch for given component.
 * @param component Component name.
 * @returns {string|null} Action type.
 */
function getActionType(component) {
    switch (component) {
        case "SMS":
            return "SHOW_SCREEN_SMS";
        case "TOKEN":
            return "SHOW_SCREEN_TOKEN";
        default:
            return null;
    }
}