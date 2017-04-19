import axios from 'axios';

export function startHandshake() {
    return function (dispatch) {
        axios.post("./api/init", {
            operationId: null
        }).then((response) => {
            if (response.data.next.length > 0) {
                var method = response.data.next[0];
                if (response.data.result === "CONFIRMED") {
                    dispatch({
                        type: "SHOW_SCREEN_LOGIN",
                        payload: {
                            loading: false,
                            error: false,
                            message: "Please log in"
                        }
                    })
                } else if (response.data.result === "FAILED") {
                    dispatch({
                        type: "SHOW_SCREEN_ERROR",
                        payload: {
                            message: response.data.message
                        }
                    })
                }
            } else {
                if (response.data.result === "CONFIRMED") {
                    dispatch({
                        type: "SHOW_SCREEN_SUCCESS",
                        payload: null
                    })
                } else if (response.data.result === "FAILED") {
                    dispatch({
                        type: "SHOW_SCREEN_ERROR",
                        payload: {
                            message: response.data.message
                        }
                    })
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
