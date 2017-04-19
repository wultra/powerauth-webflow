import axios from 'axios';

export function authenticate(username, password) {
    return function (dispatch) {
        dispatch({
            type: "SHOW_SCREEN_LOGIN",
            payload: {
                loading: true,
                error: false,
                message: "Please log in"
            }
        });
        axios.post("./api/authenticate", {
            username: username,
            password: password
        }).then((response) => {

            switch (response.data.result) {
                case 'FAILED': {
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
                case 'CONFIRMED': { //TODO: check if there are more steps to do
                    dispatch({
                        type: "SHOW_SCREEN_SUCCESS",
                        payload: response.data
                    });
                    break;
                }
            }
        }).catch((error) => {
            var errorMessage;
            if (error.response) {
                errorMessage = error.response.data.message;
            } else if (error.request) {
                errorMessage = "invalid_request"
            } else {
                errorMessage = error.message;
            }
            dispatch({
                type: "SHOW_SCREEN_ERROR",
                payload: {
                    message: errorMessage
                }
            })
        })
    }
}