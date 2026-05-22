# Used Push Message Extras

PowerAuth Cloud may send push messages to the mobile token app to support faster response on the mobile device.

Mobile app implementors can recognize messages coming from our systems by examining message "extras" (additional attributes appended to the message). More specifically, each message from our system contains the `messageType` key with values specific to a given use case.

<!-- begin box info -->
Note that push notifications work on a "best effort" basis. Applications must not rely on them; instead, apps can use notifications to respond more promptly to events in the desktop web browser (e.g., canceling the operation by clicking the "Cancel" button in the web browser).
<!-- end box -->

## Operation Lifecycle

During operation approval, our systems send the push message with the following extras:

- `messageType` (required) - message type, one of the following values:
    - `mtoken.operationInit` - in case a new operation was triggered
    - `mtoken.operationFinished` - in case the operation was finished, successfully or unsuccessfully
- `mtokenOperationResult` (required only in case of a finished operation, `messageType` = `mtoken.operationFinished`, otherwise ignored) - this key contains more information about the operation finish result, namely one of the following values:
    - `authentication.success` - the operation was successfully confirmed
    - `authentication.fail` - the operation failed to confirm
    - `operation.timeout` - the operation expired
    - `operation.canceled` - the operation was canceled by the user
    - `operation.methodNotAvailable` - (rare) mobile token authentication method was removed from the user
- `operationId` (required) - operation ID, in UUID format
- `operationName` (required) - operation name, for example, "login" or "authorize_payment"
- `activationId` (optional) - activation ID representing the activation to which the push notification was sent (if known)

## Registration Lifecycle

Whenever a registered mobile token status changes, our systems send the push message with the following extras:

- `messageType` (required) - message type, one of the following values:
    - `mtoken.statusChange` - the registration status changed, and the mobile app should poll the status
- `activationId` (required) - ID representing the activation to which the push notification was sent (if known)

## Message Inbox

When a new message is sent to the user's message inbox, our systems send the push message with the following extras:

- `messageType` (required) - message type, one of the following values:
    - `mtoken.inboxMessage.new` - a new message was posted to the inbox
- `inboxId` (required) - identifier of the inbox
