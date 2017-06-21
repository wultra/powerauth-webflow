import axios from "axios";
import {dispatchAction, dispatchError} from "../dispatcher/dispatcher";

export function getOperationData() {
    return function (dispatch) {
        axios.get("./api/auth/operation/detail").then((response) => {
            dispatch({
                type: "SHOW_SCREEN_OPERATION_DATA",
                payload: response.data
            });
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}

export function authenticate() {
    return function (dispatch) {
        axios.post("./api/auth/operation/authenticate", {}).then((response) => {
            switch (response.data.result) {
                case 'CONFIRMED': {
                    dispatchAction(dispatch, response);
                    break;
                }
                case 'FAILED': {
                    // handle timeout - action can not succeed anymore, show error
                    if (response.data.message === "authentication.timeout") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    dispatch({
                        type: "SHOW_SCREEN_ERROR",
                        payload: {
                            message: response.data.message
                        }
                    });
                    break;
                }
            }
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}