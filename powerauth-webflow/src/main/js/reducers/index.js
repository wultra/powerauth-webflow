import {combineReducers} from 'redux';

import dispatching from './dispatchingReducer'
import locale from './localeReducer'

/**
 * Combining reducer.
 */
export default combineReducers({
    dispatching,
    intl: locale
})