import axios from 'axios';
import {dispatchAction, dispatchError} from '../dispatcher/dispatcher'

export function authenticate() {
    return function (dispatch) {
        axios.post("./api/auth/init/authenticate", {}, {
            headers: {
                'X-OPERATION-HASH': operationHash,
            }
        }).then((response) => {
            // save operation hash in case the operation has been just initialized (default operation)
            if (operationHash === null) {
                operationHash = response.data.operationHash;
            }
            dispatchAction(dispatch, response);
            return null;
        }).catch((error) => {
            dispatchError(dispatch, error);
        })
    }
}
