/*
 * Copyright 2019 Wultra s.r.o.
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
 * Initialize 2FA login for given username.
 * @param username Username.
 * @param organizationId Organization ID.
 * @returns {Function} No return value.
 */
export function init(username, organizationId) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_LOGIN_2FA",
            payload: {
                loading: true,
                error: false,
                message: ""
            }
        });
        axios.post("./api/auth/login-2fa/init/start", {
            username: username,
            organizationId: organizationId
        }, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            switch (response.data.result) {
                case 'CONFIRMED': {
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
                    // handle timeout - login action can not succeed anymore, do not show login screen, show error instead
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
                        type: "SHOW_SCREEN_LOGIN_2FA",
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

export function cancel() {
    return function (dispatch) {
        axios.post("./api/auth/login-2fa/init/cancel", {}, {
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

export function getOrganizationList() {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_LOGIN_2FA",
            payload: {
                loading: true,
                error: false,
                message: ""
            }
        });
        axios.post("./api/auth/login-2fa/init/setup", {}).then((response) => {
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
                            message: "",
                        }
                    });
                }
                return null;
            }
            dispatch({
                type: "SHOW_SCREEN_LOGIN_2FA",
                payload: response.data
            });
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

export function selectOrganization(organizationId) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_LOGIN_2FA",
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