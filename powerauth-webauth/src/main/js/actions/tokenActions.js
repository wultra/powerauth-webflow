import axios from 'axios';
import { dispatchAction, dispatchError } from '../dispatcher/dispatcher'

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