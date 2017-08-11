import axios from "axios";
import {dispatchAction, dispatchError} from "../dispatcher/dispatcher";

export function init() {
    return function (dispatch) {
        axios.post("./api/auth/token/web/init", {}).then((response) => {
            dispatch({
                type: "SHOW_SCREEN_TOKEN",
                payload: response.data
            });
        }).catch((error) => {
            console.log(error);
        })
    }
}

export function authenticate(callback) {
    return function (dispatch) {
        axios.post("./api/auth/token/web/authenticate", {}).then((response) => {
            switch (response.data.result) {
                case 'CONFIRMED': {
                    callback(false);
                    dispatchAction(dispatch, response);
                    break;
                }
                case 'CANCELED': {
                    dispatch({
                        type: "SHOW_SCREEN_ERROR",
                        payload: {
                            message: response.data.message
                        }
                    });
                    break;
                }
                case 'AUTH_FAILED': {
                    // handle timeout - action can not succeed anymore, show error
                    if (response.data.message === "authentication.timeout") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    // if there is no supported auth method, show error, there is no point in continuing
                    // TODO - handle fallback - see issue #32
                    if (response.data.message === "error.noAuthMethod") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    callback(true);
                    dispatch({
                        type: "SHOW_SCREEN_TOKEN",
                        payload: {
                            info: "reload"
                        }
                    });
                    break;
                }
            }
        }).catch((error) => {
            callback(false);
            dispatchError(dispatch, error);
        })
    }
}

export function cancel() {
    return function (dispatch) {
        axios.post("./api/auth/token/web/cancel", {}).then((response) => {
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