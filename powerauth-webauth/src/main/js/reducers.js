/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

/**
 * Reducer which modifies application state based on incoming actions from backend. Old state is thrown away with
 * each incoming message.
 * @param state state to set
 * @param action current action as received from the backend
 * @returns new state
 */
function reducer(state = {sessionId:undefined}, action) {
    switch (action.type) {
        case "SAVE_ACTION":
            // simply overwrites current state
            state = action.actionState;
            break;
        case "TERMINATE_SESSION":
            // back to default state - sessionId is undefined, there is no action
            state = {sessionId:undefined};
            break;
        default:
    }
    // remove for production use
    console.log(state);
    // return new state
    return state;
}

module.exports.reducer = reducer;