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

/**
 * Dispatch an action.
 * @param dispatch Dispatch.
 * @param response Response.
 */
export function dispatchAction(dispatch, response) {
    if (response.data.next.length > 0) {
        if (response.data.result === "CONFIRMED") {
            const next = response.data.next;
            let authMethods = [];
            for (let key in next) {
                if (next.hasOwnProperty(key)) {
                    switch (next[key].authMethod) {
                        case "USERNAME_PASSWORD_AUTH": {
                            dispatch({
                                type: "SHOW_SCREEN_LOGIN",
                                payload: {
                                    loading: false,
                                    error: false,
                                    message: ""
                                }
                            });
                            break;
                        }
                        case "LOGIN_SCA": {
                            dispatch({
                                type: "SHOW_SCREEN_LOGIN_SCA",
                                payload: {
                                    loading: false,
                                    error: false,
                                    message: ""
                                }
                            });
                            break;
                        }
                        case "APPROVAL_SCA": {
                            dispatch({
                                type: "SHOW_SCREEN_APPROVAL_SCA",
                                payload: {
                                    loading: false,
                                    error: false,
                                    message: ""
                                }
                            });
                            break;
                        }
                        case "CONSENT": {
                            dispatch({
                                type: "SHOW_SCREEN_CONSENT",
                                payload: {
                                    loading: false,
                                    error: false,
                                    message: ""
                                }
                            });
                            break;
                        }
                        case "POWERAUTH_TOKEN": {
                            // add powerauth token authentication method for operation review step
                            authMethods.push("POWERAUTH_TOKEN");
                            break;
                        }
                        case "SMS_KEY": {
                            // add powerauth token authentication method for operation review step
                            authMethods.push("SMS_KEY");
                            break;
                        }
                    }
                }
            }
            if (authMethods.length > 0) {
                dispatch({
                    type: "SHOW_SCREEN_OPERATION_REVIEW",
                    payload: {
                        authMethods: authMethods
                    }
                });
            }
        } else if (response.data.result === "AUTH_FAILED") {
            dispatch({
                type: "SHOW_SCREEN_ERROR",
                payload: {
                    message: response.data.message
                }
            })
        }
    } else {
        if (response.data.result === "CONFIRMED") {
            dispatch({
                type: "SHOW_SCREEN_SUCCESS",
                payload: {
                    message: response.data.message
                }
            })
        } else if (response.data.result === "AUTH_FAILED") {
            dispatch({
                type: "SHOW_SCREEN_ERROR",
                payload: {
                    message: response.data.message
                }
            })
        }
    }
}

/**
 * Dispatch an error.
 * @param dispatch Dispatch.
 * @param error Error.
 */
export function dispatchError(dispatch, error) {
    let errorMessage;
    if (error.response) {
        if (error.response.data.responseObject.message) {
            // structured error responses which include a responseObject with a message
            errorMessage = error.response.data.responseObject.message;
        } else {
            // responses with message field in the data
            errorMessage = error.response.data.message;
        }
    } else if (error.request) {
        // the request was made but no response was received
        // see: https://www.npmjs.com/package/axios#handling-errors
        errorMessage = "message.networkError";
    } else {
        // something happened in setting up the request that triggered an Error
        // see: https://www.npmjs.com/package/axios#handling-errors
        errorMessage = error.message;
    }
    dispatch({
        type: "SHOW_SCREEN_ERROR",
        payload: {
            message: errorMessage
        }
    })
}