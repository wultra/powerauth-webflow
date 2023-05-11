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
 * Initialize OAuth 2.1 consent form.
 * @returns {Function} No return value.
 */
export function init() {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_CONSENT",
            payload: {
                loading: true,
                error: false,
                message: ""
            }
        });
        axios.post("./api/auth/consent/init", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash
            }
        }).then((response) => {
            if (response.data.result === 'AUTH_FAILED') {
                dispatchAction(dispatch, response);
                return null;
            }
            if (!response.data.shouldDisplayConsent) {
                // Skip showing of consent form and go directly to authentication
                dispatch(authenticate([], function(){}));
                return null;
            }
            dispatch({
                type: "SHOW_SCREEN_CONSENT",
                payload: {
                    loading: false,
                    error: false,
                    consentHtml: response.data.consentHtml,
                    options: response.data.options
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
 * @returns {Function} No return value.
 */
export function authenticate(options, callback) {
    return function (dispatch) {
        axios.post("./api/auth/consent/authenticate", {
            options
        }, {
            headers: {
                'X-OPERATION-HASH': operationHash
            }
        }).then((response) => {
            switch (response.data.result) {
                case 'CONFIRMED': {
                    callback();
                    dispatchAction(dispatch, response);
                    break;
                }
                case 'CANCELED':
                case 'AUTH_METHOD_FAILED': {
                    callback();
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
                            type: "SHOW_SCREEN_CONSENT",
                            payload: {
                                loading: false,
                                error: true,
                                message: response.data.message,
                                remainingAttempts: response.data.remainingAttempts,
                                consentValidationPassed: response.data.consentValidationPassed,
                                validationErrorMessage: response.data.validationErrorMessage,
                                optionValidationResults: response.data.optionValidationResults
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
export function cancel(callback) {
    return function (dispatch) {
        axios.post("./api/auth/consent/cancel", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash
            }
        }).then((response) => {
            callback();
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
