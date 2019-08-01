import {combineReducers} from 'redux';

import dispatching from './dispatchingReducer'
import locale from './localeReducer'
import security from './securityReducer'
import timeout from './timeoutReducer'

/**
 * Combining reducer.
 */
export default combineReducers({
    dispatching,
    intl: locale,
    security: security,
    timeout: timeout
})