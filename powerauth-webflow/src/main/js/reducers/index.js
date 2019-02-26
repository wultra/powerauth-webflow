import {combineReducers} from 'redux';

import dispatching from './dispatchingReducer'
import locale from './localeReducer'
import security from './securityReducer'

/**
 * Combining reducer.
 */
export default combineReducers({
    dispatching,
    intl: locale,
    security: security
})