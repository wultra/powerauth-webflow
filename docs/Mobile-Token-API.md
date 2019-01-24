# Mobile Token API

Mobile token API provides access to operations.

The generated REST API documentation in deployed Web Flow:

```
http[s]://[host]:[port]/powerauth-webflow/swagger-ui.html
```

## Mobile API Error Codes

List of error codes in Mobile Token API:

| Code | Description | HTTP Status Code |
|---|---|---|
| `INVALID_REQUEST` | Invalid request sent - missing request object in request | 400 |
| `INVALID_ACTIVATION` | Activation is not valid (it is different from configured activation) | 400 |
| `POWERAUTH_AUTH_FAIL` | PowerAuth authentication failed | 401 |
| `OPERATION_ALREADY_FINISHED` | Operation is already finished | 400 |
| `OPERATION_ALREADY_FAILED` | Operation is already failed | 400 |
| `OPERATION_ALREADY_CANCELED` | Operation is already canceled | 400 |
| `OPERATION_EXPIRED` | Operation is expired | 400 |

## Localization

In order to get a correctly localized response, please use the `Accept-Language` HTTP header in your request.

## Mobile Token API Endpoints

### Get Pending Operations

Get the list with all operations that are pending confirmation.

<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/api/auth/token/app/operation/list</code></td>
    </tr>
</table>

#### Request

- Headers:
    - `Content-Type: application/json`
    - `Accept-Language: en-US`
    - `X-PowerAuth-Token: ...`

```json
{}
```

#### Response
- Status Code: `200`
- Headers:
    - `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": [
    {
      "id": "7e0ba60f-bf22-4ff5-b999-2733784e5eaa",
      "name": "authorize_payment",
      "data": "A1*A100CZK*Q238400856/0300**D20170629*NUtility Bill Payment - 05/2017",
      "operationCreated": "2018-07-02T14:43:13+0000",
      "operationExpires": "2018-07-02T14:48:17+0000",
      "allowedSignatureType": {
        "type": "2FA",
        "variants": [
          "possession_knowledge",
          "possession_biometry"
        ]
      },
      "formData": {
        "title": "Confirm Payment",
        "message": "Hello,\nplease confirm following payment:",
        "attributes": [
          {
            "type": "HEADING",
            "id": "operation.heading",
            "label": "Utility Payment"
          },
          {
            "type": "AMOUNT",
            "id": "operation.amount",
            "label": "Amount",
            "amount": 100,
            "currency": "CZK",
            "amountFormatted": "100,00",
            "currencyFormatted": "Kč"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "value": "Jun 29, 2017"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "note": "Utility Bill Payment - 05/2017"
          },
          {
            "type": "PARTY_INFO",
            "id": "operation.partyInfo",
            "label": "Application",
            "partyInfo": {
              "logoUrl": "https://itesco.cz/img/logo/logo.svg",
              "name": "Tesco",
              "description": "Find out more about Tesco...",
              "websiteUrl": "https://itesco.cz/hello"
            }
          }
        ]
      }
    }
  ]
}
```

### Confirm Operation

Confirms an operation with given ID and data. This endpoint requires a signature of a type specified by the operation.

<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/api/auth/token/app/operation/authorize</code></td>
    </tr>
</table>

#### Request

- Headers:
    - `Content-Type: application/json`
    - `X-PowerAuth-Authorization: ...`

```json
{
  "requestObject": {
    "id": "3699a9c0-45f0-458d-84bc-5bde7ec384f7",
    "data": "A1*A100CZK*Q238400856\/0300**D20170629*NUtility Bill Payment - 05\/2017"
  }
}
```

#### Response
- Status Code: `200`
- Headers:
    - `Content-Type: application/json`

```json
{
  "status": "OK"
}
```

### Reject Operation

Reject an operation with given ID, with a provided reason.

<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/api/auth/token/app/operation/cancel</code></td>
    </tr>
</table>

#### Request

- Headers:
    - `Content-Type: application/json`
    - `X-PowerAuth-Authorization: ...`

```json
{
  "requestObject": {
    "id": "352d6cfa-b8d7-4366-af1f-c99b071b4dc4",
    "reason": "INCORRECT_DATA"
  }
}
```

#### Response
- Status Code: `200`
- Headers:
    - `Content-Type: application/json`

```json
{
  "status": "OK"
}
```

## Enumerations

### Form Attribute Types

| Type | Description |
|---|---|
| `AMOUNT` | Form field representing an amount with currency. |
| `KEY_VALUE` | Form field representing a key value item, where items are displayed next to each other. This realistically impose limitation on value length - it should fit into the single line. |
| `NOTE` | Form field representing a generic text note, where label is displayed above the note. As a result, note can be of an arbitrary length and can be multi-line. |
| `HEADING` | Form field representing a heading, where label is displayed as the heading text. |

### Operation Rejection Reasons

| Type | Description |
|---|---|
| `UNKNOWN` | User decided not to tell us the operation rejection reason. |
| `INCORRECT_DATA` | User claims incorrect data was presented in mToken app. |
| `UNEXPECTED_OPERATION` | User claims he/she did not expect any operation. |

### Allowed Signature Types

| Type | Description |
|---|---|
| `1FA` | One-factor signature - user just has to tap "Confirm" button to confirm it. |
| `2FA` | Two-factor signature - user needs to use either password of biometry as addition to possession factor. The `variants` key then determines what signature type is allowed for the given operation. |
| `ECDSA` | ECDSA signature with device private key. |
