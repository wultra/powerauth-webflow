import axios from "axios";
import {dispatchAction, dispatchError} from "../dispatcher/dispatcher";

export function init() {
    return function (dispatch) {
        axios.post("./api/auth/token/init", {}).then((response) => {
        }).catch((error) => {
        })
    }
}

export function authenticate(callback) {
    return function (dispatch) {
        axios.post("./api/auth/token/authenticate", {}).then((response) => {
            switch (response.data.result) {
                case 'CONFIRMED': {
                    callback(false);
                    dispatchAction(dispatch, response);
                    break;
                }
                case 'FAILED': {
                    // handle timeout - action can not succeed anymore, show error
                    if (response.data.message === "authentication.timeout") {
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