import { combineReducers } from 'redux';

import usernamePassword from './usernamePasswordReducer'
import dispatching from './dispatchingReducer'

export default combineReducers({
    usernamePassword,
    dispatching
})