export default function reducer(state = { error: null }, action) {
    switch (action.type) {
        case "AUTHENTICATE_REJECTED": {
            return {...state, error: action.payload }
        }
    }
    return state;
}