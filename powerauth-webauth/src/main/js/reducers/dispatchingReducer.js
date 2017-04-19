export default function reducer(state = { currentScreen: "SCREEN_START_HANDSHAKE", context: null }, action) {
    switch (action.type) {
        case "SHOW_SCREEN_LOGIN": {
            return {... state, currentScreen: "SCREEN_LOGIN", context: action.payload};
        }
        case "SHOW_SCREEN_SUCCESS": {
            return {... state, currentScreen: "SCREEN_SUCCESS", context: action.payload};
        }
        case "SHOW_SCREEN_ERROR": {
            return {... state, currentScreen: "SCREEN_ERROR", context: action.payload};
        }
    }
    return state;
}