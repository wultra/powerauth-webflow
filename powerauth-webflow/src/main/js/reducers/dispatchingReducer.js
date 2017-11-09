export default function reducer(state = {currentScreen: "SCREEN_START_HANDSHAKE", context: null}, action) {
    switch (action.type) {
        case "SHOW_SCREEN_LOGIN": {
            return {...state, currentScreen: "SCREEN_LOGIN", context: action.payload};
        }
        case "SHOW_SCREEN_OPERATION_REVIEW": {
            return {
                ...state,
                currentScreen: "SCREEN_OPERATION_REVIEW",
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "SHOW_SCREEN_TOKEN": {
            return {
                ...state,
                currentScreen: "SCREEN_TOKEN",
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "SHOW_SCREEN_SMS": {
            return {
                ...state,
                currentScreen: "SCREEN_SMS",
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "SHOW_SCREEN_SUCCESS": {
            return {...state, currentScreen: "SCREEN_SUCCESS", context: action.payload};
        }
        case "SHOW_SCREEN_ERROR": {
            return {...state, currentScreen: "SCREEN_ERROR", context: action.payload};
        }
        case "CHANGE_BANK_ACCOUNT": {
            return {
                ...state,
                // do not change the current screen
                currentScreen: state.currentScreen,
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "CHANGE_ACTIVATION": {
            return {
                ...state,
                // do not change the current screen
                currentScreen: state.currentScreen,
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "CHOOSE_AUTH_METHOD": {
            return {
                ...state,
                // do not change the current screen
                currentScreen: state.currentScreen,
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
    }
    return state;
}

/**
 * Merges old and new context to preserve data related to the operation which should be loaded only once.
 * @param actionType action type
 * @param oldContext old context from which data is taken
 * @param newContext new context into which data is inserted
 * @returns {*} new context
 */
function mergeContext(actionType, oldContext, newContext) {
    if (oldContext === null) {
        // nothing to do
        return newContext;
    }
    switch (actionType) {
        case "SHOW_SCREEN_OPERATION_REVIEW":
            mergeAuthMethods(oldContext, newContext);
            mergeData(oldContext, newContext);
            break;
        case "SHOW_SCREEN_TOKEN":
            mergeData(oldContext, newContext);
            break;
        case "SHOW_SCREEN_SMS":
            mergeData(oldContext, newContext);
            break;
        case "CHANGE_BANK_ACCOUNT":
            changeBankAccount(oldContext, newContext);
            break;
        case "CHANGE_ACTIVATION":
            changeActivation(oldContext, newContext);
            break;
    }
    return newContext;
}

function mergeData(oldContext, newContext) {
    // formData need to remain in context
    if (oldContext.formData !== undefined && newContext.formData === undefined) {
        newContext.formData = oldContext.formData;
    }
    // operation data needs to remain in context
    if (oldContext.data !== undefined && newContext.data === undefined) {
        newContext.data = oldContext.data;
    }
}

function mergeAuthMethods(oldContext, newContext) {
    // authMethods need to remain in context
    if (oldContext.authMethods !== undefined && newContext.authMethods === undefined) {
        newContext.authMethods = oldContext.authMethods;
    }
}

function changeBankAccount(oldContext, newContext) {
    let chosenBankAccountNumber = newContext.chosenBankAccountNumber;
    // copy all oldContext properties except for chosenBankAccountNumber, which should be taken from newContext
    for (const prop in oldContext) {
        if (oldContext.hasOwnProperty(prop)) {
            newContext[prop] = oldContext[prop];
        }
    }
    newContext.formData.userInput.chosenBankAccountNumber = chosenBankAccountNumber;
}

function changeActivation(oldContext, newContext) {
    let chosenActivationId = newContext.chosenActivation.activationId;
    // copy all oldContext properties except for chosenActivationId, which should be taken from newContext
    for (const prop in oldContext) {
        if (oldContext.hasOwnProperty(prop)) {
            newContext[prop] = oldContext[prop];
        }
    }
    newContext.formData.userInput.chosenActivationId = chosenActivationId;
}
