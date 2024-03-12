# Used Push Message Extras

During the Mobile Token authentication and authorization, PowerAuth Web Flow may send the following push messages to the Mobile Token app:

- `messageType` (required) - message type, one of the following values:
    - `mtoken.operationInit` - in case a new operation was triggered
    - `mtoken.operationFinished` - in case the operation was finished, successfully or non-successfully
- `mtokenOperationResult` (required only in case of a finished operation, `messageType` = `mtoken.operationFinished`, otherwise ignored) - this key contains more information about the operation finish result, namely one of the following values:
    - `authentication.success` - the operation was successfully confirmed
    - `authentication.fail` - the operation failed to confirm
    - `operation.timeout` - the operation expired
    - `operation.canceled` - the operation was canceled by the user
    - `operation.methodNotAvailable` - (rare) mobile token authentication method was removed from the user
- `operationId` (required) - operation ID, in UUID format
- `operationName` (required) - operation name, for example, "login" or "authorize_payment"
- `activationId` (optional) - activation ID representing the activation to which the push notification was sent (if known)

Please note that push notifications work on the "best effort" principle, so applications must not rely on them. For example, apps can use notifications to respond more promptly to events on the desktop web browser (i.e., canceling the operation by clicking the "Cancel" button on the web).
