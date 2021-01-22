# Used Push Message Extras

During the Mobile Token authentication and authorization, PowerAuth Web Flow may send following push messages to the Mobile Token app:

- `messageType` (required) - message type, one of the following values:
    - `mtoken.operationInit` - in case new operation was triggerred
    - `mtoken.operationFinished` - in case operation was finished, successfully or non-successfully
- `mtokenOperationResult` (required only in case of a finished operation, `messageType` = `mtoken.operationFinished`, otherwise ignored) - this key contains more information about the operation finish result, namely one of the following values:
    - `authentication.success` - operation was successfully confirmed
    - `authentication.fail` - operation failed to confirm
    - `operation.timeout` - operation expired
    - `operation.canceled` - operation was cancelled by the user
    - `operation.methodNotAvailable` - (rare) mToken authentication method was removed from the user
- `operationId` (required) - operation ID, in UUID format
- `operationName` (required) - operation name, for example "login" or "authorize_payment"

Please note that push notifications work on "best effort" principle and therefore, application must not rely on them. It can use the notification for example to deliver more prompt response on events in desktop web browser.
