# Mobile Push Registration API

In order to register mobile device to the push notifications, following endpoints are published.

## Endpoints

### Register for Push Notifications

Registers a device to push notifications.

<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/api/push/device/register</code></td>
    </tr>
</table>

#### Request

- Headers:
    - `Content-Type: application/json`
    - `X-PowerAuth-Token: ...`

```json
{
  "requestObject": {
      "platform": "ios",
      "token": "10de0b9c-791f-4e9f-93c4-e2203951c307"
  }
}
```

Supported platforms:
- `ios`
- `android`

#### Success Response
- Status Code: `200`
- Headers:
    - `Content-Type: application/json`

```json
{
  "status": "OK"
}
```

#### Error Response - Push Registration Failed
- Returned when Push Server returns error during registration.
- Status Code: `400`
- Headers:
    - `Content-Type: application/json`

```json
{
    "status": "ERROR",
    "responseObject": {
        "code": "PUSH_REGISTRATION_FAILED",
        "message": "Push registration failed in Mobile Token API component."
    }
}
```

#### Error Response - PowerAuth Authentication Failed
- Returned when PowerAuth authentication fails.
- Status Code: `401`
- Headers:
    - `Content-Type: application/json`

```json
{
    "status": "ERROR",
    "responseObject": {
        "code": "POWERAUTH_AUTH_FAIL",
        "message": "Unable to verify device registration"
    }
}
```

#### Error Response - Invalid Request Object
- Returned when request object is invalid.
- Status Code: `400`
- Headers:
    - `Content-Type: application/json`

```json
{
    "status": "ERROR",
    "responseObject": {
        "code": "INVALID_REQUEST",
        "message": "Invalid request object sent to Mobile Token API component."
    }
}
```

#### Error Response - Invalid Activation
- Returned when application or activation is invalid.
- Status Code: `400`
- Headers:
    - `Content-Type: application/json`

```json
{
    "status": "ERROR",
    "responseObject": {
        "code": "INVALID_ACTIVATION",
        "message": "Invalid activation found in Mobile Token API component."
    }
}
```
