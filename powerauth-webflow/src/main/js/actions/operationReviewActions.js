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
import {dispatchError} from "../dispatcher/dispatcher";

export function getOperationData() {
    return function (dispatch) {
        axios.get("./api/auth/operation/detail").then((response) => {
            if (response.data.formData && response.data.formData.userInput.chosenAuthMethod) {
                // if authMethod is already chosen, skip choice and go directly to the authMethod
                switch (response.data.formData.userInput.chosenAuthMethod) {
                    case "POWERAUTH_TOKEN":
                        dispatch({
                            type: "SHOW_SCREEN_TOKEN",
                            payload: response.data
                        });
                        return;
                    case "SMS_KEY":
                        dispatch({
                            type: "SHOW_SCREEN_SMS",
                            payload: response.data
                        });
                        return;
                    // otherwise show regular operation review with authMethod choice
                }
            }
            dispatch({
                type: "SHOW_SCREEN_OPERATION_REVIEW",
                payload: response.data
            });
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

export function cancel() {
    return function (dispatch) {
        axios.post("./api/auth/operation/cancel", {}).then((response) => {
            dispatch({
                type: "SHOW_SCREEN_ERROR",
                payload: {
                    message: response.data.message
                }
            });
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

export function chooseAuthMethod(authMethod) {
    return function (dispatch) {
        dispatch({
            type: "CHOOSE_AUTH_METHOD",
            payload: {
                chosenAuthMethod: authMethod
            }
        });
    }
}

export function updateFormData(formData, callback) {
    return function (dispatch) {
        axios.put("./api/auth/operation/formData", {
            formData: formData
        }).then((response) => {
            callback();
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}
