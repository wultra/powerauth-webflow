export default function reducer(state = { currentScreen: "SCREEN_START_HANDSHAKE", context: null }, action) {
    switch (action.type) {
        case "SHOW_SCREEN_LOGIN": {
            return {... state, currentScreen: "SCREEN_LOGIN", context: action.payload};
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
            return {... state, currentScreen: "SCREEN_SUCCESS", context: action.payload};
        }
        case "SHOW_SCREEN_ERROR": {
            return {... state, currentScreen: "SCREEN_ERROR", context: action.payload};
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
            // authMethods need to remain in context for operation review
            if (oldContext.authMethods !== undefined) {
                newContext.authMethods = oldContext.authMethods;
            }
        // break not used - logic below also belongs to the SHOW_SCREEN_OPERATION_REVIEW action
        case "SHOW_SCREEN_TOKEN":
        case "SHOW_SCREEN_SMS":
            // displayDetails need to remain in context for operation review and authorization methods
            if (oldContext.displayDetails !== undefined) {
                newContext.displayDetails = oldContext.displayDetails;
            }
            // operation data needs to remain in context for operation review and authorization methods
            if (oldContext.data !== undefined) {
                newContext.data = oldContext.data;
            }
            break;
    }
    return newContext;
}
