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

function checkAccess(props, component) {
    if (props.sessionId === undefined) {
        return false;
    }
    if (props.action === undefined) {
        return false;
    }
    switch (component) {
        case "login":
            if (props.action === "DISPLAY_LOGIN_FORM") {
                return true;
            }
            break;
        case "payment-info":
            if (props.action === "DISPLAY_PAYMENT_INFO") {
                return true;
            }
            break;
        case "authorize":
            if (props.action === "DISPLAY_PAYMENT_AUTHORIZATION_FORM") {
                return true;
            }
            break;
        case "message":
            if (props.action === "DISPLAY_MESSAGE") {
                return true;
            }
            break;
        case "terminate":
            if (props.action === "TERMINATE") {
                return true;
            }
            if (props.action === "TERMINATE_REDIRECT") {
                return true;
            }
            break;
    }
    return false;
}

function saveAction(actionState) {
    console.log(actionState);
    return {
        type: "SAVE_ACTION",
        actionState
    }
}

function terminateSession(sessionId) {
    return {
        type: "TERMINATE_SESSION",
        sessionId
    }
}

module.exports.checkAccess = checkAccess;
module.exports.saveAction = saveAction;
module.exports.terminateSession = terminateSession;
