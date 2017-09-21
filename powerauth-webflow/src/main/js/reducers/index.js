import {combineReducers} from 'redux';

import dispatching from './dispatchingReducer'
import locale from './localeReducer'

export default combineReducers({
    dispatching,
    intl: locale
})