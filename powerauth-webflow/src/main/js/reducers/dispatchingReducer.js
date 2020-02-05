/**
 * Reducer for screen navigation.
 * @param state Current state.
 * @param action Action.
 * @returns {{currentScreen: string, context: null}} New state.
 */
export default function reducer(state = {currentScreen: "SCREEN_START_HANDSHAKE", context: null}, action) {
    switch (action.type) {
        case "SHOW_SCREEN_LOGIN": {
            return {
                ...state,
                currentScreen: "SCREEN_LOGIN",
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "SHOW_SCREEN_LOGIN_SCA": {
            return {
                ...state,
                currentScreen: "SCREEN_LOGIN_SCA",
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "SHOW_SCREEN_APPROVAL_SCA": {
            return {
                ...state,
                currentScreen: "SCREEN_APPROVAL_SCA",
                context: mergeContext(action.type, state.context, action.payload)
            };
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
        case "SHOW_SCREEN_CONSENT": {
            return {
                ...state,
                currentScreen: "SCREEN_CONSENT",
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "SHOW_SCREEN_SUCCESS": {
            return {...state, currentScreen: "SCREEN_SUCCESS", context: action.payload};
        }
        case "SHOW_SCREEN_ERROR": {
            return {...state, currentScreen: "SCREEN_ERROR", context: action.payload};
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
        case "SHOW_SCREEN_LOGIN":
            mergeOrganizations(oldContext, newContext);
            break;
        case "SHOW_SCREEN_LOGIN_SCA":
            mergeOrganizations(oldContext, newContext);
            mergeCertificateData(oldContext, newContext);
            break;
        case "SHOW_SCREEN_OPERATION_REVIEW":
            mergeAuthMethods(oldContext, newContext);
            mergeData(oldContext, newContext);
            break;
        case "SHOW_SCREEN_TOKEN":
        case "SHOW_SCREEN_SMS":
            mergeData(oldContext, newContext);
            break;
        case "SHOW_SCREEN_CONSENT":
            mergeConsent(oldContext, newContext);
            break;
    }
    return newContext;
}

function mergeOrganizations(oldContext, newContext) {
    // organizations need to remain in context
    if (oldContext.organizations !== undefined && newContext.organizations === undefined) {
        newContext.organizations = oldContext.organizations;
    }
    // chosenOrganizationId need to remain in context
    if (oldContext.chosenOrganizationId !== undefined && newContext.chosenOrganizationId === undefined) {
        newContext.chosenOrganizationId = oldContext.chosenOrganizationId;
    }
}

function mergeCertificateData(oldContext, newContext) {
    // clientCertificateAuthenticationEnabled need to remain in context
    if (oldContext.clientCertificateAuthenticationEnabled !== undefined && newContext.clientCertificateAuthenticationEnabled === undefined) {
        newContext.clientCertificateAuthenticationEnabled = oldContext.clientCertificateAuthenticationEnabled;
    }
    // clientCertificateVerificationUrl need to remain in context
    if (oldContext.clientCertificateVerificationUrl !== undefined && newContext.clientCertificateVerificationUrl === undefined) {
        newContext.clientCertificateVerificationUrl = oldContext.clientCertificateVerificationUrl;
    }
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

function mergeConsent(oldContext, newContext) {
    // consent need to remain in context
    if (oldContext.consent !== undefined && newContext.consent === undefined) {
        newContext.consent = oldContext.consent;
    }
}
