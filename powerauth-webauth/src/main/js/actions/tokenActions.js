import axios from 'axios';
import { dispatchAction, dispatchError } from '../dispatcher/dispatcher'

export function authenticate() {
    return function (dispatch) {
        axios.post("./api/auth/token/authenticate", {}).then((response) => {
            switch (response.data.result) {
                case 'CONFIRMED': {
                    dispatchAction(dispatch, response);
                    break;
                }
                case 'FAILED': {
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
            dispatchError(dispatch, error);
        })
    }
}