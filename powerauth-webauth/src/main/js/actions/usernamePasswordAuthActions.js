import axios from "axios";
import {dispatchAction, dispatchError} from "../dispatcher/dispatcher";

export function authenticate(username, password) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_LOGIN",
            payload: {
                loading: true,
                error: false,
                message: "login.pleaseLogIn" //TODO: Update message to "Loading..."
            }
        });
        axios.post("./api/auth/form/authenticate", {
            username: username,
            password: password
        }).then((response) => {
            switch (response.data.result) {
                case 'CONFIRMED': {
                    dispatchAction(dispatch, response);
                    break;
                }
                case 'FAILED': {
                    // handle timeout - login action can not succeed anymore, do not show login screen, show error instead
                    if (response.data.message === "authentication.timeout") {
                        dispatchAction(dispatch, response);
                        break;
                    }
                    dispatch({
                        type: "SHOW_SCREEN_LOGIN",
                        payload: {
                            loading: false,
                            error: true,
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
