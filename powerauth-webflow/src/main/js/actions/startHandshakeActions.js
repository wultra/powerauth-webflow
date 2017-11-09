import axios from 'axios';
import {dispatchAction, dispatchError} from '../dispatcher/dispatcher'

export function authenticate() {
    return function (dispatch) {
        axios.post("./api/auth/init/authenticate", {}).then((response) => {
            dispatchAction(dispatch, response);
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}
