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
 * Initialize SCA approval.
 * @returns {Function} No return value.
 */
export function init(callback) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_APPROVAL_SCA",
            payload: {
                loading: true,
                error: false,
                message: ""
            }
        });
        axios.post("./api/auth/approval-sca/init", {}).then((response) => {
            dispatch({
                type: "SHOW_SCREEN_APPROVAL_SCA",
                loading: true,
                error: false,
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
                type: "SHOW_SCREEN_APPROVAL_SCA",
                loading: false,
                error: false,
                payload: response.data
            });
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

/**
 * Confirm SCA approval.
 * @returns {Function} No return value.
 */
export function confirm() {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_APPROVAL_SCA",
            payload: {
                loading: true,
                error: false,
                message: ""
            }
        });
        axios.post("./api/auth/approval-sca/authenticate", {}, {
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
                    if (!handleAuthFailedError(dispatch, response)) {
                        dispatch({
                            type: "SHOW_SCREEN_APPROVAL_SCA",
                            payload: {
                                loading: false,
                                error: true,
                                message: response.data.message
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
 * Cancel SCA approval.
 * @returns {Function} No return value.
 */
export function cancel() {
    return function (dispatch) {
        axios.post("./api/auth/approval-sca/cancel", {}, {
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
