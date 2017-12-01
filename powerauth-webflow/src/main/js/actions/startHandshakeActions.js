import axios from 'axios';
import {dispatchAction, dispatchError} from '../dispatcher/dispatcher'

export function authenticate() {
    return function (dispatch) {
        axios.post("./api/auth/init/authenticate", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            dispatchAction(dispatch, response);
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}
