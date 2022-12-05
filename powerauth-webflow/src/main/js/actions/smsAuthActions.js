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
                // Handle error when initialization fails.
                dispatch({
                    type: getActionType(component),
                    payload: {
                        loading: false,
                        error: true,
                        init: true,
                        message: response.data.message,
                        passwordEnabled: response.data.passwordEnabled,
                        smsOtpEnabled: response.data.smsOtpEnabled,
                        certificateEnabled: response.data.certificateEnabled,
                        username: response.data.username,
                        resendDelay: response.data.resendDelay,
                        signatureDataBase64: response.data.signatureDataBase64
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
                    certificateEnabled: response.data.certificateEnabled,
                    username: response.data.username,
                    resendDelay: response.data.resendDelay,
                    signatureDataBase64: response.data.signatureDataBase64
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
 * @param signedMessage Message signed using qualified certificate.
 * @param component Component requesting the action.
 * @returns {Function} No return value.
 */
export function authenticate(userAuthCode, userPassword, signedMessage, component) {
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
            password: userPassword,
            signedMessage: signedMessage
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
            // Handle request validation errors
            if (error.response.status === 400 && error.response.data.message !== undefined) {
                dispatch({
                    type: getActionType(component),
                    payload: {
                        loading: false,
                        error: true,
                        message: error.response.data.message,
                        remainingAttempts: error.response.data.remainingAttempts
                    }
                });
                return;
            }
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
 * Initialize the ICA signer library.
 */
export function initializeICAClientSign() {
    if (!document.getElementById('ICAPKIService')) {
        const head = document.head;
        const script = document.createElement('script');
        script.src = './resources/signer/ica/ICAPKIService.js';
        script.type = 'text/javascript';
        script.id = 'ICAPKIService';
        head.appendChild(script);
        script.onload = () => {
            const script = document.createElement('script');
            script.src = './resources/signer/ica/ICAClientSign.js';
            script.type = 'text/javascript';
            script.id = 'ICAClientSign';
            head.appendChild(script);
            script.onload = () => {
                const script = document.createElement('script');
                script.src = './resources/signer/ica/control.js';
                script.type = 'text/javascript';
                script.id = 'control';
                head.appendChild(script);
                script.onload = () => {
                    const script = document.createElement('script');
                    script.src = './resources/signer/ica/signer.js';
                    script.type = 'text/javascript';
                    script.id = 'signer_init';
                    head.appendChild(script);
                }
            }
        }
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