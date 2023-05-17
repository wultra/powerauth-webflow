/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
 * Authenticate SCA login for given username.
 * @param username Username.
 * @param organizationId Organization ID.
 * @returns {Function} No return value.
 */
export function authenticate(username, organizationId) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_LOGIN_SCA",
            payload: {
                loading: true,
                error: false,
                message: ""
            }
        });
        axios.post("./api/auth/login-sca/authenticate", {
            username: username,
            organizationId: organizationId
        }, {
            headers: {
                'X-OPERATION-HASH': operationHash
            }
        }).then((response) => {
            switch (response.data.result) {
                case 'CONFIRMED': {
                    if (response.data.next.length > 0) {
                        // Step was completely authenticated, move to the next step
                        dispatchAction(dispatch, response);
                        break;
                    }
                    if (response.data.mobileTokenEnabled) {
                        dispatch({
                            type: "SHOW_SCREEN_TOKEN",
                            payload: {
                                loading: true,
                                error: false,
                                message: "",
                                smsFallbackAvailable: true
                            }
                        });
                    } else {
                        dispatch({
                            type: "SHOW_SCREEN_SMS",
                            payload: {
                                loading: true,
                                error: false,
                                message: ""
                            }
                        });
                    }
                    break;
                }
                case 'AUTH_FAILED': {
                    if (!handleAuthFailedError(dispatch, response)) {
                        dispatch({
                            type: "SHOW_SCREEN_LOGIN_SCA",
                            payload: {
                                loading: false,
                                error: true,
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
                    type: "SHOW_SCREEN_LOGIN_SCA",
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
 * Initialize SCA login.
 * @returns {Function} No return value.
 */
export function initLoginSca(callback) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_LOGIN_SCA",
            payload: {
                loading: true,
                error: false,
                message: ""
            }
        });
        axios.post("./api/auth/login-sca/init", {}).then((response) => {
            // Handling of page refresh
            if (response.data.userAlreadyKnown) {
                if (response.data.mobileTokenEnabled) {
                    dispatch({
                        type: "SHOW_SCREEN_TOKEN",
                        payload: {
                            loading: true,
                            error: false,
                            message: "",
                            smsFallbackAvailable: true
                        }
                    });
                } else {
                    dispatch({
                        type: "SHOW_SCREEN_SMS",
                        payload: {
                            loading: true,
                            error: false,
                            message: ""
                        }
                    });
                }
                return null;
            }
            dispatch({
                type: "SHOW_SCREEN_LOGIN_SCA",
                payload: response.data
            });
            callback(true);
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Cancel SCA login.
 * @returns {Function} No return value.
 */
export function cancel() {
    return function (dispatch) {
        axios.post("./api/auth/login-sca/cancel", {}, {
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
 * Select an organization.
 * @param organizationId Organization ID.
 * @returns {Function} No return value.
 */
export function selectOrganization(organizationId) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_LOGIN_SCA",
            payload: {
                chosenOrganizationId: organizationId
            }
        });
    }
}

/**
 * Interrupt operation and show unexpected error about missing organization configuration.
 * @returns {Function} Missing organization configuration error is dispatched.
 */
export function organizationConfigurationError() {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_ERROR",
            payload: {
                message: "organization.configurationError"
            }
        })
    }
}

/**
 * Verify client TLS certificate.
 * @param certificateVerificationUrl URL to be used to verify client TLS certificate.
 * @param callbackOnSuccess Callback in case of successful verification.
 * @returns {Function} No return value.
 */
export function checkClientCertificate(certificateVerificationUrl, callbackOnSuccess) {
    return function (dispatch) {
        axios.post(certificateVerificationUrl, {}, {
            // Send cookies so that HTTP session is the same
            withCredentials: true
        }).then((response) => {
            callbackOnSuccess();
            return null;
        }).catch((error) => {
            // Convert error message to a user friendly error message
            dispatch({
                type: "SHOW_SCREEN_LOGIN_SCA",
                payload: {
                    loading: false,
                    error: true,
                    message: "clientCertificate.failed"
                }
            });
            return null;
        })
    }
}
