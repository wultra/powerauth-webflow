# Data Adapter RESTful API Reference

PowerAuth Web Flow server communicates with the Data Adapter via a REST API. This chapter defines the REST API implemented by Data Adapter and consumed by the Web Flow Server.

Following topics are covered in this chapter:
- [Status codes and error handling](#status-codes-and-error-handling)
- [Service status](#service-status)
- [User lookup](#user-lookup)
- [User authentication](#user-authentication)
- [User information](#user-information)
- [Decorate form data](#decorate-form-data)
- [Form data change notification](#formdata-change-notification)
- [Operation change notification](#operation-change-notification)
- [Generate authorization SMS](#generate-sms-authorization-code)
- [Verify authorization SMS code](#verify-authorization-sms-code)
- [Verify authorization SMS code and password](#verify-authorization-sms-code-and-password)
- [Create OAuth 2.0 consent form](#create-oauth-20-consent-form)
- [Validate OAuth 2.0 consent form](#validate-oauth-20-consent-form)
- [Save OAuth 2.0 consent form](#save-oauth-20-consent-form)

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
  "status": "OK",
  "responseObject": {
    "applicationName": "powerauth-data-adapter",
    "applicationDisplayName": "PowerAuth Data Adapter",
    "applicationEnvironment": "ENV1",
    "version": "0.22.0",
    "buildTime": "2019-06-21T12:49:22.959+0000",
    "timestamp": "2019-06-21T13:05:49.740+0000"
  }
}
```

- `applicationName` - Application name.
- `applicationDisplayName` - Application display name.
- `applicationEnvironment` - Application environment.
- `version` - Version of Data Adapter.
- `buildTime` - Time when the powerauth-data-adapter.war file was built.
- `timestamp` - Response timestamp.

## User Lookup

Performs a lookup of user account.

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/auth/user/lookup</code></td>
	</tr>
</table>

The list of expected status codes during authentication:

| Code | Description |
|------|-------------|
| 200  | OK response - user details were successfully retrieved |
| 400  | `INPUT_INVALID` - username has invalid format |
| 400  | `USER_NOT_FOUND` - user account does not exist |
| 400  | `REMOTE_ERROR` - communication with remote system failed |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

```json
{
  "requestObject": {
    "username": "user1234",
    "organizationId": "RETAIL",
    "operationContext": {
      "id": "7662c638-9dc9-484c-a119-145b3685e623",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {}
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "applicationOwner": "Wultra",
          "requestedScopes": [
            "OAUTH"
          ]
        }
      }
    }
  }
}
```

### Response - user account exists

```json
{
  "status": "OK",
  "responseObject": {
    "id": "12345678",
    "givenName": "John",
    "familyName": "Doe",
    "organizationId": "RETAIL"
  }
}
```

### Response - user account does not exist

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "USER_NOT_FOUND",
    "message": "login.userNotFound",
    "validationErrors": null,
    "remainingAttempts": null
  }
}
```

### Response - invalid input

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "INPUT_INVALID",
    "message": "login.username.empty",
    "validationErrors": [
      "login.username.empty.objectRequest.requestObject.username",
      "login.username.empty.requestObject.username",
      "login.username.empty.username",
      "login.username.empty.java.lang.String",
      "login.username.empty"
    ],
    "remainingAttempts": null
  }
}
```

## User Authentication

Performs an authentication operation with user ID and password.

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
| 400  | `INPUT_INVALID` - username and/or password has invalid format, unsupported authentication type |
| 400  | `REMOTE_ERROR` - communication with remote system failed |
| 401  | `AUTHENTICATION_FAILED` - provide reason in the message in case it is available |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "12345678",
    "password": "s3cret",
    "organizationId": "RETAIL",
    "authenticationType": "BASIC",
    "cipherTransformation": null,
    "operationContext": {
      "id": "447fbd89-6f46-46da-a573-ade4f3409c94",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {}
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "applicationOwner": "Wultra",
          "requestedScopes": [
            "OAUTH"
          ]
        }
      }
    }
  }
}
```

### User Password Encryption and Decryption

The `BASIC` authentication type is used for plain text password in request (sent by default). 

The password can be optionally encrypted. In this case the `authenticationType` parameter provides information about
type of password encryption and the `cipherTransformation` parameter contains information about used cipher.

It is expected that the remote system which handles password verification decrypts the password and it is not required to decrypt 
the password in the Data Adapter itself. The following code sample decrypts the encrypted password for `PASSWORD_ENCRYPTION_AES` authentication type:

```java
// Read secret key from configuration
String secretKeyBase64 = configuration.getSecretKey();
byte[] secretKeyBytes = BaseEncoding.base64().decode(secretKey);
String secretKey = SecretKeySpec(secretKeyBytes, "AES");

// Extract cipher transformation and encrypted password from request
String cipherTransformation = request.getCipherTransformation();
String password = request.getPassword();

// Extract IV and encrypted password and convert them to bytes
String[] parts = password.split(":");
if (parts.length != 2) {
    throw new IllegalArgumentException("Invalid request");
}
String ivBase64 = parts[0];
byte ivBytes = BaseEncoding.base64().decode(ivBase64);
String encryptedPasswordBase64 = parts[1];
byte encryptedPasswordBytes = BaseEncoding.base64().decode(encryptedPasswordBase64);

// Decrypt password using specified cipher transformation, extracted IV and encrypted password bytes
Cipher cipher = Cipher.getInstance(cipherTransformation);
cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));
byte[] decryptedPasswordBytes = cipher.doFinal(encryptedPasswordBytes);
String decryptedPassword = new String(decrypted, StandardCharsets.UTF_8);

// Verify that decrypted password matches expected password
```

### Response - authentication succeeded

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": {
    "userId": "12345678",
    "organizationId": "RETAIL"
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
| 400  | `INPUT_INVALID` - request validation errors |
| 400  | `USER_NOT_FOUND` - user account does not exist |
| 400  | `REMOTE_ERROR` - communication with remote system failed |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "12345678",
    "organizationId": "RETAIL"
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
    "id": "12345678",
    "givenName": "John",
    "familyName": "Doe",
    "organizationId": "RETAIL"
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
| 400  | `INPUT_INVALID` - request validation errors |
| 400  | `USER_NOT_FOUND` - user account does not exist |
| 400  | `REMOTE_ERROR` - communication with remote system failed |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "12345678",
    "organizationId": "RETAIL",
    "operationContext": {
      "id": "371526cc-5dee-414e-8418-5ee1c5ef2d67",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
        }
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "applicationOwner": "Wultra",
          "requestedScopes": [
            "OAUTH"
          ]
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
          "formattedValues": {
            "amount": "100.00",
            "currency": "CZK"
          },
          "amount": 100,
          "currency": "CZK",
          "currencyId": "operation.currency"
        },
        {
          "type": "KEY_VALUE",
          "id": "operation.account",
          "label": "To Account",
          "valueFormatType": "ACCOUNT",
          "formattedValues": {
            "value": "238400856/0300"
          },
          "value": "238400856/0300"
        },
        {
          "type": "KEY_VALUE",
          "id": "operation.dueDate",
          "label": "Due Date",
          "valueFormatType": "DATE",
          "formattedValues": {
            "value": "Jun 29, 2017"
          },
          "value": "2017-06-29"
        },
        {
          "type": "NOTE",
          "id": "operation.note",
          "label": "Note",
          "valueFormatType": "TEXT",
          "formattedValues": {
            "value": "Utility Bill Payment - 05/2017"
          },
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
| 400  | `INPUT_INVALID` - request validation errors |
| 400  | `REMOTE_ERROR` - communication with remote system failed |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "12345678",
    "organizationId": "RETAIL",
    "operationContext": {
      "id": "371526cc-5dee-414e-8418-5ee1c5ef2d67",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678"
        }
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "applicationOwner": "Wultra",
          "requestedScopes": [
            "OAUTH"
          ]
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
  "status": "OK"
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
| 400  | `INPUT_INVALID` - request validation errors |
| 400  | `REMOTE_ERROR` - communication with remote system failed |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Request

Possible operation changes are: `DONE`, `CANCELED` and `FAILED`.

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "12345678",
    "organizationId": "RETAIL",
    "operationContext": {
      "id": "c02c5ea8-d4da-4499-9a27-aa4ded70921b",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678",
          "operation.bankAccountChoice.disabled": "true"
        }
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "applicationOwner": "Wultra",
          "requestedScopes": [
            "OAUTH"
          ]
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
  "status": "OK"
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
| 400  | `INPUT_INVALID` - request validation errors |
| 400  | `OPERATION_CONTEXT_INVALID` - invalid operation context |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Create SMS - request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "12345678",
    "organizationId": "RETAIL",
    "operationContext": {
      "id": "371526cc-5dee-414e-8418-5ee1c5ef2d67",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678",
          "operation.bankAccountChoice.disabled": "true"
        }
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "applicationOwner": "Wultra",
          "requestedScopes": [
            "OAUTH"
          ]
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
| 400  | `INPUT_INVALID` - request validation errors  |
| 401  | `SMS_AUTHORIZATION_FAILED` - the SMS authorization code is invalid |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Verify SMS - request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "messageId": "617178ab-f315-4223-a602-9d4893b4f99f",
    "authorizationCode": "77038183",
    "operationContext": {
      "id": "371526cc-5dee-414e-8418-5ee1c5ef2d67",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678",
          "operation.bankAccountChoice.disabled": "true"
        }
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "applicationOwner": "Wultra",
          "requestedScopes": [
            "OAUTH"
          ]
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
  "status": "OK"
}
```

## Verify Authorization SMS Code and Password

### Verify SMS code and password - request parameters

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/auth/sms/password/verify</code></td>
	</tr>
</table>

The list of expected status codes:

| Code | Description |
|------|-------------|
| 200  | OK response - SMS authorization code has been successfully verified |
| 400  | `INPUT_INVALID` - request validation errors  |
| 401  | `SMS_AUTHORIZATION_FAILED` - the SMS authorization code is invalid |
| 401  | `AUTHENTICATION_FAILED` - password verification failed |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

The password can be optionally encrypted. In this case the `authenticationType` parameter provides information about
type of encryption and the `cipherTransformation` parameter contains information about used cipher.

See chapter [User Password Encryption and Decryption](./Data-Adapter-REST-API-Reference.md#user-password-encryption-and-decryption) for additional details.

### Verify SMS and password - request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "12345678",
    "password": "s3cret",
    "organizationId": "RETAIL",
    "authenticationType": "BASIC",
    "cipherTransformation": null,  
    "messageId": "617178ab-f315-4223-a602-9d4893b4f99f",
    "authorizationCode": "77038183",
    "operationContext": {
      "id": "371526cc-5dee-414e-8418-5ee1c5ef2d67",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678",
          "operation.bankAccountChoice.disabled": "true"
        }
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "applicationOwner": "Wultra",
          "requestedScopes": [
            "OAUTH"
          ]
        }
      }
    }
  }
}
```

### Response - SMS authorization code and password have been successfully verified

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK"
}
```

## Create OAuth 2.0 Consent Form

### Create consent form - request parameters

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/auth/consent/create</code></td>
	</tr>
</table>

The list of expected status codes:

| Code | Description |
|------|-------------|
| 200  | OK response - consent form has been successfully created |
| 400  | `INPUT_INVALID` - request validation errors |
| 400  | `OPERATION_CONTEXT_INVALID` - invalid operation context |
| 400  | `REMOTE_ERROR` - communication with remote system failed |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Create consent form - request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "12345678",
    "operationContext": {
      "id": "7d92fce2-c1f2-4d5b-b522-61da0749fdf7",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678",
          "operation.bankAccountChoice.disabled": "true"
        }
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "requestedScopes": [
            "OAUTH"
          ],
          "applicationOwner": "Wultra"
        }
      }
    },
    "lang": "en"
  }
}
```

### Response - consent form has been successfully created

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": {
    "consentHtml": "I consent that I have initiated this payment request and give consent to complete the operation.",
    "options": [
      {
        "id": "CONSENT_INIT",
        "descriptionHtml": "I consent that I have initiated this payment operation.",
        "required": true,
        "defaultValue": "NOT_CHECKED",
        "value": null
      },
      {
        "id": "CONSENT_PAYMENT",
        "descriptionHtml": "I give consent to complete this payment operation.",
        "required": true,
        "defaultValue": "NOT_CHECKED",
        "value": null
      }
    ]
  }
}
```

## Validate OAuth 2.0 Consent Form

### Validate consent form - request parameters

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/auth/consent/validate</code></td>
	</tr>
</table>

The list of expected status codes:

| Code | Description |
|------|-------------|
| 200  | OK response - consent form has been successfully validated (however it may contain validation errors) |
| 400  | `INPUT_INVALID` - request validation errors |
| 400  | `OPERATION_CONTEXT_INVALID` - invalid operation context |
| 400  | `CONSENT_DATA_INVALID` - invalid consent data |
| 400  | `REMOTE_ERROR` - communication with remote system failed |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Validate consent form - request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "12345678",
    "operationContext": {
      "id": "7d92fce2-c1f2-4d5b-b522-61da0749fdf7",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678",
          "operation.bankAccountChoice.disabled": "true"
        }
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "requestedScopes": [
            "OAUTH"
          ],
          "applicationOwner": "Wultra"
        }
      }
    },
    "lang": "en",
    "options": [
      {
        "id": "CONSENT_INIT",
        "descriptionHtml": "I consent that I have initiated this payment operation.",
        "required": true,
        "defaultValue": "NOT_CHECKED",
        "value": null
      },
      {
        "id": "CONSENT_PAYMENT",
        "descriptionHtml": "I give consent to complete this payment operation.",
        "required": true,
        "defaultValue": "NOT_CHECKED",
        "value": null
      }
    ]
  }
}
```

### Response - consent form has been successfully validated

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": {
    "consentValidationPassed": true,
    "validationErrorMessage": null,
    "optionValidationResults": []
  }
}
```

### Response - consent form options validation failed

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": {
    "consentValidationPassed": false,
    "validationErrorMessage": "Please fill in the whole consent form.",
    "optionValidationResults": [
      {
        "id": "CONSENT_INIT",
        "validationPassed": false,
        "errorMessage": "Confirm this option to complete the operation."
      },
      {
        "id": "CONSENT_PAYMENT",
        "validationPassed": false,
        "errorMessage": "Confirm this option to complete the operation."
      }
    ]
  }
}
```

## Save OAuth 2.0 Consent Form

### Save consent form - request parameters

<table>
	<tr>
		<td>Method</td>
		<td><code>POST</code></td>
	</tr>
	<tr>
		<td>Resource URI</td>
		<td><code>/api/auth/consent/save</code></td>
	</tr>
</table>

The list of expected status codes:

| Code | Description |
|------|-------------|
| 200  | OK response - consent form has been successfully saved |
| 400  | `INPUT_INVALID` - request validation errors |
| 400  | `OPERATION_CONTEXT_INVALID` - invalid operation context |
| 400  | `CONSENT_DATA_INVALID` - invalid consent data |
| 400  | `REMOTE_ERROR` - communication with remote system failed |
| 500  | Server errors - provide error details in the message, this is only for unexpected errors |

### Save consent form - request

- Headers:
	- `Content-Type: application/json`

```json
{
  "requestObject": {
    "userId": "12345678",
    "operationContext": {
      "id": "4a04667b-8a1a-46af-813c-cf71ffcde478",
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
            "formattedValues": {
              "amount": "100.00",
              "currency": "CZK"
            },
            "amount": 100,
            "currency": "CZK",
            "currencyId": "operation.currency"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.account",
            "label": "To Account",
            "valueFormatType": "ACCOUNT",
            "formattedValues": {
              "value": "238400856/0300"
            },
            "value": "238400856/0300"
          },
          {
            "type": "KEY_VALUE",
            "id": "operation.dueDate",
            "label": "Due Date",
            "valueFormatType": "DATE",
            "formattedValues": {
              "value": "Jun 29, 2017"
            },
            "value": "2017-06-29"
          },
          {
            "type": "NOTE",
            "id": "operation.note",
            "label": "Note",
            "valueFormatType": "TEXT",
            "formattedValues": {
              "value": "Utility Bill Payment - 05/2017"
            },
            "note": "Utility Bill Payment - 05/2017"
          }
        ],
        "userInput": {
          "operation.bankAccountChoice": "CZ4012340000000012345678",
          "operation.bankAccountChoice.disabled": "true"
        }
      },
      "applicationContext": {
        "id": "DEMO",
        "name": "Demo application",
        "description": "Web Flow demo application",
        "extras": {
          "requestedScopes": [
            "OAUTH"
          ],
          "applicationOwner": "Wultra"
        }
      }
    },
    "options": [
      {
        "id": "CONSENT_INIT",
        "descriptionHtml": "I consent that I have initiated this payment operation.",
        "required": true,
        "defaultValue": "NOT_CHECKED",
        "value": "CHECKED"
      },
      {
        "id": "CONSENT_PAYMENT",
        "descriptionHtml": "I give consent to complete this payment operation.",
        "required": true,
        "defaultValue": "NOT_CHECKED",
        "value": "CHECKED"
      }
    ]
  }
}
```

### Response - consent form has been successfully saved

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": {
    "saveSucceeded": true
  }
}
```

### Response - the processing was correct however consent form could not be saved at this time

- Status Code: `200`
- Headers:
	- `Content-Type: application/json`

```json
{
  "status": "OK",
  "responseObject": {
    "saveSucceeded": false
  }
}
```
