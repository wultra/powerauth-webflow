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

export function dispatchAction(dispatch, response) {
    if (response.data.next.length > 0) {
        if (response.data.result === "CONFIRMED") {
            var method = response.data.next[0];
            switch (method.authMethod) {
                case "USERNAME_PASSWORD_AUTH": {
                    dispatch({
                        type: "SHOW_SCREEN_LOGIN",
                        payload: {
                            loading: false,
                            error: false,
                            message: "login.pleaseLogIn"
                        }
                    });
                    break;
                }
                case "SHOW_OPERATION_DETAIL": {
                    dispatch({
                        type: "SHOW_SCREEN_OPERATION_DATA",
                        payload: {
                            data: ""
                        }
                    });
                    break;
                }
            }
        } else if (response.data.result === "FAILED") {
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
                payload: null
            })
        } else if (response.data.result === "FAILED") {
            dispatch({
                type: "SHOW_SCREEN_ERROR",
                payload: {
                    message: response.data.message
                }
            })
        }
    }
}

export function dispatchError(dispatch, error) {
    var errorMessage;
    if (error.response) {
        errorMessage = error.response.data.message;
    } else if (error.request) {
        errorMessage = "message.invalidRequest"
    } else {
        errorMessage = error.message;
    }
    dispatch({
        type: "SHOW_SCREEN_ERROR",
        payload: {
            message: errorMessage
        }
    })
}