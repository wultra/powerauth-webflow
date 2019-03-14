# Data Adapter RESTful API Reference

PowerAuth Web Flow server communicates with the Data Adapter via a REST API. This chapter defines the REST API implemented by Data Adapter and consumed by the Web Flow Server.

Following topics are covered in this chapter:
- [Status codes and error handling](#status-codes-and-error-handling)
- [Service status](#service-status)
- [User authentication](#user-authentication)
- [User information](#user-information)
- [Decorate form data](#decorate-form-data)
- [Form data change notification](#formdata-change-notification)
- [Operation change notification](#operation-change-notification)
- [Generate authorization SMS](#generate-sms-authorization-code)
- [Verify authorization SMS code](#verify-authorization-sms-code)

You can access the generated REST API documentation in deployed Data Adapter:

```
http[s]://[host]:[port]/powerauth-data-adapter/swagger-ui.html
```

## Status codes and error handling

PowerAuth compliant Data Adapter uses a unified format for error response body, accompanied with an appropriate HTTP status code. Besides the HTTP error codes that application server may return regardless of server application (such as 404 when resource is not found or 503 when server is down).

All error responses that are produced by the Data Adapter should have following body:

```json
{
    "status": "ERROR",
    "responseObject": {
        "code": "ERROR_CODE",
        "message": "ERROR_MESSAGE_I18N_KEY"
    }
}
```

Expected error messages are explained in details in individual sections.

## Service Status

Get a system status response, with basic information about the running application.

<table>
<tr>
<td>Method</td>
<td><code>GET</code></td>
</tr>
<tr>
<td>Resource URI</td>
<td>/api/service/status</td>
</tr>
</table>

#### **Response**

```json
{
    "status" : "OK",
    "responseObject": {
        "applicationName" : "powerauth-data-adapter",
        "applicationDisplayName" : "PowerAuth Data Adapter",
        "applicationEnvironment" : "",
        "version": "0.20.0",
        "buildTime": "2017-03-11T11:24:33Z",
        "timestamp" : "2017-03-14T14:54:14Z"
    }  
}
```

- `applicationName` - Application name.
- `applicationDisplayName` - Application display name.
- `applicationEnvironment` - Application environment.
- `version` - Version of Data Adapter.
- `buildTime` - Time when the powerauth-data-adapter.war file was built.
- `timestamp` - Response timestamp.

## User Authentication

Performs an authentication operation with username and password.

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/auth/user/authenticate</code></td>
	</tr>
</table>

The list of expected status codes during authentication:

| Code | Description |
|------|-------------|
| 200  | OK response - user was successfully authenticated |
| 400  | Invalid input - username and/or password has invalid format, unsupported authentication type |
| 401  | Authentication failed - provide reason in the message in case it is available |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "username": "userxyz",
    "password": "s3cret",
    "organizationId": "RETAIL",
    "type": "BASIC",
    "operationContext": {
      "id": "feaec766-1b44-42cb-9872-596a4fed689f",
      "name": "authorize_payment",
      "data": "A1*A100CZK*Q238400856/0300**D20170629*NUtility Bill Payment - 05/2017",
      "formData": {
        "title": {
          "id": "operation.title",
          "message": "Confirm Payment"
        },
        "greeting": {
          "id": "operation.greeting",
          "message": "Hello,\nplease confirm following payment:"
        },
        "summary": {
          "id": "operation.summary",
          "message": "Hello, please confirm payment 100 CZK to account 238400856/0300."
        },
        "config": [],
        "banners": [],
        "parameters": [
          {
            "type": "AMOUNT",
            "id": "operation.amount",
            "label": "Amount",
            "valueFormatType": "AMOUNT",
            "formattedValue": "100.00 CZK",
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValue": "238400856/0300",
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValue": "Jun 29, 2017",
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValue": "Utility Bill Payment - 05/2017",
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {}
      }
    }
  }
}
```

* The only currently supported authentication method is BASIC, however this field is present for future extensions of the API.

### Response - authentication succeeded

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
    "status": "OK",
    "responseObject": {
        "userId": "12345678"
    }
}
```

The userId value is a system-wide unique identifier identifying the user who was just authenticated.

### Response - authentication failed

This message should be sent when the Data Adapter receives a correct message, however the username and password combination is invalid.

- Status Code: `401`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "AUTHENTICATION_FAILED",
    "message": "login.authenticationFailed",
    "validationErrors": null,
    "remainingAttempts": 2
  }
}
```

### Response - input validation errors

This error should be returned when username or password format is invalid - either it contains unsupported characters or it is empty or too long. This error is also used when authentication type is not supported.

- Status Code: `400`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "INPUT_INVALID",
    "message": "login.username.empty login.password.empty",
    "validationErrors": [
      "login.username.empty.objectRequest.requestObject.username",
      "login.username.empty.requestObject.username",
      "login.username.empty.username",
      "login.username.empty.java.lang.String",
      "login.username.empty",
      "login.password.empty.objectRequest.requestObject.password",
      "login.password.empty.requestObject.password",
      "login.password.empty.password",
      "login.password.empty.java.lang.String",
      "login.password.empty"
    ],
    "remainingAttempts": 3
  }
}
```

For more information, see classes `AuthenticationRequestValidator` and `DefaultExceptionResolver`.

### Response - internal error

This error should be used for all unexpected errors.

- Status Code: `500`
- Headers:
	- `Content-Type: application/json`

```json
{
    "status": "ERROR",
    "responseObject": {
        "code": "ERROR_GENERIC",
        "message": "Exception occurred at ...",
        "validationErrors": null,
        "remainingAttempts": 3
    }
}
```

## User Information

Fetches user details based on user ID.

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/auth/user/info</code></td>
	</tr>
</table>

The list of expected status codes:

| Code | Description |
|------|-------------|
| 200  | OK response - user details have been successfully retrieved |
| 400  | Invalid request - validation errors, user not found |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

- Headers:
	- `Content-Type: application/json`

```json
{
    "requestObject": {
        "id": "12345678"
    }
}
```

### Response - user info successfully retrieved

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
    "status": "OK",
    "responseObject": {
         "id":"12345678",
         "givenName":"John",
         "familyName":"Doe"
    }
}
```

## Decorate Form Data

Retrieve form data and decorate it (optional).

### Request parameters

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/operation/formdata/decorate</code></td>
	</tr>
</table>

The list of expected status codes:

| Code | Description |
|------|-------------|
| 200  | OK response - form data was successfully decorated |
| 400  | Invalid request - user not found |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "roman",
    "organizationId": "RETAIL",    
    "operationContext": {
      "id": "52710b20-86ab-40d0-be07-8d59a765150d",
      "name": "authorize_payment",
      "data": "A1*A100CZK*Q238400856/0300**D20170629*NUtility Bill Payment - 05/2017",
      "formData": {
        "title": {
          "id": "operation.title",
          "message": "Confirm Payment"
        },
        "greeting": {
          "id": "operation.greeting",
          "message": "Hello,\nplease confirm following payment:"
        },
        "summary": {
          "id": "operation.summary",
          "message": "Hello, please confirm payment 100 CZK to account 238400856/0300."
        },
        "config": [],
        "banners": [],
        "parameters": [
          {
            "type": "AMOUNT",
            "id": "operation.amount",
            "label": "Amount",
            "valueFormatType": "AMOUNT",
            "formattedValue": "100.00 CZK",
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValue": "238400856/0300",
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValue": "Jun 29, 2017",
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValue": "Utility Bill Payment - 05/2017",
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
        }
      }
    }
  }
}
```

### Response

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": {
    "formData": {
      "title": {
        "id": "operation.title",
        "message": "Confirm Payment"
      },
      "greeting": {
        "id": "operation.greeting",
        "message": "Hello,\nplease confirm following payment:"
      },
      "summary": {
        "id": "operation.summary",
        "message": "Hello, please confirm payment 100 CZK to account 238400856/0300."
      },
      "config": [],
      "banners": [],
      "parameters": [
        {
          "type": "AMOUNT",
          "id": "operation.amount",
          "label": "Amount",
          "valueFormatType": "AMOUNT",
          "formattedValue": "100.00 CZK",
          "amount": 100,
          "currency": "CZK",
          "currencyId": "operation.currency"
        },
        {
          "type": "KEY_VALUE",
          "id": "operation.account",
          "label": "To Account",
          "valueFormatType": "ACCOUNT",
          "formattedValue": "238400856/0300",
          "value": "238400856/0300"
        },
        {
          "type": "KEY_VALUE",
          "id": "operation.dueDate",
          "label": "Due Date",
          "valueFormatType": "DATE",
          "formattedValue": "Jun 29, 2017",
          "value": "2017-06-29"
        },
        {
          "type": "NOTE",
          "id": "operation.note",
          "label": "Note",
          "valueFormatType": "TEXT",
          "formattedValue": "Utility Bill Payment - 05/2017",
          "note": "Utility Bill Payment - 05/2017"
        },
        {
          "type": "BANK_ACCOUNT_CHOICE",
          "id": "operation.bankAccountChoice",
          "label": null,
          "bankAccounts": [
            {
              "number": "12345678/1234",
              "accountId": "CZ4012340000000012345678",
              "name": "Běžný účet v CZK",
              "balance": 24394.52,
              "currency": "CZK",
              "usableForPayment": false,
              "unusableForPaymentReason": null
            },
            {
              "number": "87654321/4321",
              "accountId": "CZ4043210000000087654321",
              "name": "Spořící účet v CZK",
              "balance": 158121.10,
              "currency": "CZK",
              "usableForPayment": false,
              "unusableForPaymentReason": null
            },
            {
              "number": "44444444/1111",
              "accountId": "CZ4011110000000044444444",
              "name": "Spořící účet v EUR",
              "balance": 1.90,
              "currency": "EUR",
              "usableForPayment": false,
              "unusableForPaymentReason": "Low account balance"
            }
          ],
          "enabled": true,
          "defaultValue": "CZ4012340000000012345678"
        }
      ],
      "userInput": {
      }
    }
  }
}
```

## FormData Change Notification

Notification of Data Adapter about formData change.

### Request parameters

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/operation/formdata/change</code></td>
	</tr>
</table>

The list of expected status codes:

| Code | Description |
|------|-------------|
| 200  | OK response - notification was successfully received |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "roman",
    "organizationId": "RETAIL",    
    "operationContext": {
      "id": "38511d38-f4de-4e50-a9ab-2d176d6a8cd4",
      "name": "authorize_payment",
      "data": "A1*A100CZK*Q238400856/0300**D20170629*NUtility Bill Payment - 05/2017",
      "formData": {
        "title": {
          "id": "operation.title",
          "message": "Confirm Payment"
        },
        "greeting": {
          "id": "operation.greeting",
          "message": "Hello,\nplease confirm following payment:"
        },
        "summary": {
          "id": "operation.summary",
          "message": "Hello, please confirm payment 100 CZK to account 238400856/0300."
        },
        "config": [],
        "banners": [],
        "parameters": [
          {
            "type": "AMOUNT",
            "id": "operation.amount",
            "label": "Amount",
            "valueFormatType": "AMOUNT",
            "formattedValue": "100.00 CZK",
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValue": "238400856/0300",
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValue": "Jun 29, 2017",
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValue": "Utility Bill Payment - 05/2017",
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678"
        }
      }
    },
    "formDataChange": {
      "type": "BANK_ACCOUNT_CHOICE",
      "bankAccountId": "CZ4012340000000012345678"
    }
  }
}
```

### Response

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": null
}
```

## Operation Change Notification

Notification of Data Adapter about operation change.

### Request parameters

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/operation/change</code></td>
	</tr>
</table>

The list of expected status codes:

| Code | Description |
|------|-------------|
| 200  | OK response - notification was successfully received |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

Possible operation changes are: `DONE`, `CANCELED` and `FAILED`.

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "roman",
    "organizationId": "RETAIL",    
    "operationContext": {
      "id": "63046cce-731b-4a0d-89ef-5ff18c07e1d9",
      "name": "authorize_payment",
      "data": "A1*A100CZK*Q238400856/0300**D20170629*NUtility Bill Payment - 05/2017",
      "formData": {
        "title": {
          "id": "operation.title",
          "message": null
        },
        "greeting": {
          "id": "operation.greeting",
          "message": null
        },
        "summary": {
          "id": "operation.summary",
          "message": null
        },
        "config": [],
        "banners": [],
        "parameters": [
          {
            "type": "AMOUNT",
            "id": "operation.amount",
            "label": null,
            "valueFormatType": "AMOUNT",
            "formattedValue": null,
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": null,
            "valueFormatType": "ACCOUNT",
            "formattedValue": null,
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": null,
            "valueFormatType": "DATE",
            "formattedValue": null,
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": null,
            "valueFormatType": "TEXT",
            "formattedValue": null,
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678",
          "operation.bankAccountChoice.disabled": "true"
        }
      }
    },
    "operationChange": "DONE"
  }
}
```

### Response

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": null
}
```

## Generate SMS Authorization Code

### Generate SMS authorization code - request parameters

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/auth/sms/create</code></td>
	</tr>
</table>

The list of expected status codes:

| Code | Description |
|------|-------------|
| 200  | OK response - SMS message has been successfully created |
| 400  | Invalid request - the request validation failed |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Create SMS - request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "roman",
    "organizationId": "RETAIL",    
    "operationContext": {
      "id": "817db0c4-2d07-4ab4-86b3-b94ba10cd5b8",
      "name": "authorize_payment",
      "data": "A1*A100CZK*Q238400856/0300**D20170629*NUtility Bill Payment - 05/2017",
      "formData": {
        "title": {
          "id": "operation.title",
          "message": "Confirm Payment"
        },
        "greeting": {
          "id": "operation.greeting",
          "message": "Hello,\nplease confirm following payment:"
        },
        "summary": {
          "id": "operation.summary",
          "message": "Hello, please confirm payment 100 CZK to account 238400856/0300."
        },
        "config": [],
        "banners": [],
        "parameters": [
          {
            "type": "AMOUNT",
            "id": "operation.amount",
            "label": "Amount",
            "valueFormatType": "AMOUNT",
            "formattedValue": "100.00 CZK",
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValue": "238400856/0300",
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValue": "Jun 29, 2017",
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValue": "Utility Bill Payment - 05/2017",
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678",
          "operation.bankAccountChoice.disabled": "true"
        }
      }
    },
    "lang": "en"
  }
}
```

### Response - SMS has been successfully created

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": {
    "messageId": "884de880-925d-47a9-8ff9-1954bf990de1"
  }
}
```

## Verify Authorization SMS Code

### Verify SMS code - request parameters

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/auth/sms/verify</code></td>
	</tr>
</table>

The list of expected status codes:

| Code | Description |
|------|-------------|
| 200  | OK response - SMS authorization code has been successfully verified |
| 400  | Invalid request - the request validation failed |
| 401  | Unauthorized - the SMS authorization code is invalid |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Verify SMS - request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "messageId": "884de880-925d-47a9-8ff9-1954bf990de1",
    "authorizationCode": "26415730",
    "operationContext": {
      "id": "817db0c4-2d07-4ab4-86b3-b94ba10cd5b8",
      "name": "authorize_payment",
      "data": "A1*A100CZK*Q238400856/0300**D20170629*NUtility Bill Payment - 05/2017",
      "formData": {
        "title": {
          "id": "operation.title",
          "message": "Confirm Payment"
        },
        "greeting": {
          "id": "operation.greeting",
          "message": "Hello,\nplease confirm following payment:"
        },
        "summary": {
          "id": "operation.summary",
          "message": "Hello, please confirm payment 100 CZK to account 238400856/0300."
        },
        "config": [],
        "banners": [],
        "parameters": [
          {
            "type": "AMOUNT",
            "id": "operation.amount",
            "label": "Amount",
            "valueFormatType": "AMOUNT",
            "formattedValue": "100.00 CZK",
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValue": "238400856/0300",
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValue": "Jun 29, 2017",
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValue": "Utility Bill Payment - 05/2017",
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678",
          "operation.bankAccountChoice.disabled": "true"
        }
      }
    }
  }
}
```

### Response - SMS authorization code has been successfully verified

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": null
}
```