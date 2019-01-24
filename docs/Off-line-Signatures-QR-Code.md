# Off-line Signature QR Code

## Table of contents

- [Introduction](#introduction)
- [Operation attributes](#operation-attributes)
- [Operation data](#operation-data)
  - [Header](#header)
  - [Known template types](#known-template-types)
  - [Data types](#data-types)
  - [Data fields](#data-fields)
- [Flags](#flags)
- [Signature calculation](#signature)
- [Template details](#template-details)
  - [`0` Generic](#0-generic)
  - [`1` Payment](#1-payment)
  - [`2` Login request](#2-login-request)
- [Forward compatibility](#forward-compatibility)
- [How to generate QR codes](#how-to-generate-qr-codes)

## Introduction

This chapter describes how operation data is encoded into QR code for the purpose of off-line verification.


## Operation attributes

The format of QR code for offline operations is a high density string composed from several main operation attributes:

1. `{OPERATION_ID}` - operation identifier (UUID like identifier)
2. `{TITLE}` - title for operation, in UTF8 format. For example: "Payment".
   - ASCII control characters (code < 32) are forbidden
   - `\n` can be used for newline character
   - `\\` can be used for backslash
3. `{MESSAGE}` - message associated with operation, in UTF8 format. For example: "Please confirm this playment"
   - ASCII control characters (code < 32) are forbidden
   - `\n` can be used for newline character
   - `\\` can be used for backslash
4. `{OPERATION_DATA}` - content of operation data
5. `{FLAGS}` - various flags affecting how operation is processed
6. `{NONCE_B64}` - nonce, 16 random bytes in Base64 format. Required for pure offline signature.
7. `{SIGNING_KEY_TYPE}{SIGNATURE_B64}` - ECDSA signature in Base64 format. The signature string has an one prefix character, containing an information about signing key type.

Then, the final string for QR code is simple, new-line separated list of attributes:

```
{OPERATION_ID}\n
{TITLE}\n
{MESSAGE}\n
{OPERATION_DATA}\n
{FLAGS}\n
{NONCE_B64}\n
{SIGNING_KEY_TYPE}{SIGNATURE_B64}
```

For example:
```
5ff1b1ed-a3cc-45a3-8ab0-ed60950312b6
Payment
Please confirm this payment
A1*A100CZK*ICZ2730300000001165254011*D20180425
B
AD8bOO0Df73kNaIGb3Vmpg==
0MEYCIQDby1Uq+MaxiAAGzKmE/McHzNOUrvAP2qqGBvSgcdtyjgIhAMo1sgqNa1pPZTFBhhKvCKFLGDuHuTTYexdmHFjUUIJW=
```

## Operation data

Operation data is an asterisk separated list of fields, where the first field defines a version of operation data and template. Other fields are additional and contains typically significant attributes, which has to be presented to the user, before the operation is confirmed and signed.

Each field has its unique type, defined by first letter. For example, following string contains header `A1` and three additional fields: "Amount", "Account" and "Date":
```
A1*A100CZK*ICZ2730300000001165254011*D20180425
```

### Header

Header is composed from two parts:
* `{VERSION}{TEMPLATE}`, where
  * `{VERSION}` is an one capital letter defining version of operation data. First version is `A`, then `B`, `C`, etc...
  * `{TEMPLATE}` is a decimal number defining a template which helps with other fields interpretation.

Version `A` has following limitations:
  * Templates from `0` up to `99` are supported
  * Up to 5 additional fields are supported

### Known template types

* `0` - generic template. Each field will be displayed with "default" title.
* `1` - payment data
* `2` - confirm login

If template defines an optional field and this field is not present in operation data, then an empty string can be used. For example, following strings defines various forms of payment:
- `A1*A100CZK*ICZ2730300000001165254011***` - all optional parameters are empty
- `A1*A100CZK*ICZ2730300000001165254011` - the same as above, but with omitted asterisks
- `A1*A100CZK*ICZ2730300000001165254011***Nnote for recipient` - Last note is optional but used, so asterisks must be used to put note field at the right position

Note that templates other than `0` may have an implicit `{TITLE}` and `{MESSAGE}` attributes. For example, it's not required to issue "Payment" and "Please confirm this payment" titles, when the "payment" template is used. In this case, simple empty newline is used for both data attributes. This rule unfortunately goes agains our forward compatibility principle, so it's recommended only for version `A` templates.

### Data types

This section defines data types available for data fields:

* `{DECIMAL}` - a decimal number with dot as decimal separator. The Examples:
  * `100.10` - number with a fractional part
  * `1492` - number without a fractional part

* `{CURRENCY}` - Currency code from [ISO 4217](https://en.wikipedia.org/wiki/ISO_4217)

* `{IBAN}` - Fully qualified IBAN with optional, comma separated BIC. For example:
  * `CZ2730300000001165254011` - IBAN without BIC code
  * `CZ2730300000001165254011,AIRACZPP` - IBAN with BIC code

* `{TEXT}` - UTF-8 encoded text with few escaped characters:
  * `\n` - newline
  * `\\` - escape for backslash
  * `\*` - escape for asterisk
  * All characters with ASCII code < 32 are forbidden (e.g. `\t` should not be in the string)
  * In the QR code generator, it is recommended to compress spaces. That means that two and more consequent space characters
    should be replaced with just one space character.


### Data fields

* `A{DECIMAL}{CURRENCY}` - **Amount** with currency.
  * `A100CZK`
  * `A1492.50EUR`
  * **"Amount"** is a default title, when template is not recognized.

* `I{IBAN}` - **Counter account** in IBAN format, with optional BIC code:
  * `ICZ2730300000001165254011`
  * `ICZ2730300000001165254011,AIRACZPP`
  * **"Account"** is a default title, when template is not recognized.

* `Q{TEXT}` - **Counter account** in arbitrary format:
  * `Q1165254011/3030` - an example for czech account
  * **"Account"** is a default title, when template is not recognized.

* `D{DATE}` - **date field** in `YYYYMMDD` format. For example:
  * `D20180425`
  * **"Date"** is a default title

* `R{TEXT}` - **Operation's reference**. This field contains a general reference associated with the operation.
  For example, for payment it can be a payment reference or in case of czech domestic payment, for trasmission of
  symbols asscociated with payment:
  * `RID3343432434` - some payment reference
  * `R/VS123456/SS345/KS` - Czech specific, defines: VS=123456, SS=345, KS=empty
  * **"Reference"** is a default title

* `N{TEXT}` - **Note** associated with the operation. It can be for example note associated with the payment.
  * `NZa vecerne pivo`
  * **"Note"** is a default title

* `T{TEXT}` - **Arbitrary textual** field, displayed as is, without any additional processing.
  * `TRate 1EUR = 25,49CZK`
  * **Attribute N**, is a default title, where N is an auto incremented number, starting with 1. For example, if you use two `T` fields in data, then the first in row will have "Attribute 1" and second "Attributer 2" title.

## Flags

`{FLAGS}` attribute is a string of characters, where each character represents one flag. Order of characters in string is not important.

| Flag | Meaning                      |
|------|:-----------------------------|
| `B`  | Operation can be signed with biometric factor |

Examples:
- `B` - biometric 2FA is allowed
- ` ` (empty string) - only knowledge factor is allowed for 2FA

## Signature

Signature attribute is composed from two separate fields:
- `{SIGNING_KEY_TYPE}` is one character defining which key was used for signature calculation. Available options are:
   - `0` - `KEY_SERVER_MASTER_PRIVATE` was used for ECDSA signature calculation
   - `1` - `KEY_SERVER_PRIVATE` personalized key was used for ECDSA signature calculation
- `{SIGNATURE_B64}` is ECDSA signature calculated with selected private key.

Then the signed data payload is composed as:

```
{OPERATION_ID}\n
{TITLE}\n
{MESSAGE}\n
{OPERATION_DATA}\n
{FLAGS}\n
{NONCE_B64}\n
{SIGNING_KEY_TYPE}
```

## Template details

### `0` Generic

Generic template has no predefined order of fields. You can use up to 5 fields with any type in the operation data. Note that `{TITLE}` and `{MESSAGE}` data attributes are required for this kind of template.


### `1` Payment

*Available since version `A`*

| Attribute | Title                        |
|-----------|:-----------------------------|
| Title     | Payment                      |
| Message   | Please confirm this payment  |

Data fields

| # | Type | Title                        | Required |
|---|:------:|:-----------------------------|:--------:|
| 1 | A      | Amount                       | yes      |
| 2 | I or Q | Counter account              | yes      |
| 3 | R      | Payment Reference (or parsed symbols in CZ) | |
| 4 | D      | Due date                     |          |
| 5 | N      | Note                         |          |

Example data:
```
5ff1b1ed-a3cc-45a3-8ab0-ed60950312b6
Domestic payment

A1*A100CZK*ICZ2730300000001165254011*R/VS123456/SS/KS*D20180425

AD8bOO0Df73kNaIGb3Vmpg==
0MEYCIQDby1Uq+MaxiAAGzKmE/McHzNOUrvAP2qqGBvSgcdtyjgIhAMo1sgqNa1pPZTFBhhKvCKFLGDuHuTTYexdmHFjUUIJW=
```


### `2` Login request

*Available since version `A`*

| Attribute | Title                        |
|-----------|:-----------------------------|
| Title     | Login request                |
| Message   | Please confirm login into *internet banking.* |

Data fields for this type of teplate are not specified, so any available fields will be interpreted as for generic template.

Example data:
```
5ff1b1ed-a3cc-45a3-8ab0-ed60950312b6


A2
B
AD8bOO0Df73kNaIGb3Vmpg==
0MEYCIQDby1Uq+MaxiAAGzKmE/McHzNOUrvAP2qqGBvSgcdtyjgIhAMo1sgqNa1pPZTFBhhKvCKFLGDuHuTTYexdmHFjUUIJW=
```

## Forward compatibility

The data format is designed with forward compatibility in mind. This means that QR codes issued in newer data format can be processed in older data parsers. This is possible due to following contract rules:

- Rules for operation attributes:
  - `{NONCE_B64}`, `{SIGNING_KEY_TYPE}`, `{SIGNATURE_B64}` are always last attributes in the string and
    ECDSA signature is always calculated for all attributes listed before the signature attribute.
  - If a new operation attribute needs to be added, then it has to be inserted as a new line, before `{NONCE_B64}`.
  - For Example:
    ```
    {OPERATION_ID}\n
    {TITLE}\n
    {MESSAGE}\n
    {OPERATION_DATA}\n
    {FLAGS}\n
    {XXX_NEW_ATTRIBUTE}\n
    {NONCE_B64}\n
    {SIGNING_KEY_TYPE}{SIGNATURE_B64}
    ```
    then, the signature will be calculated from following data:
    ```
    {OPERATION_ID}\n
    {TITLE}\n
    {MESSAGE}\n
    {OPERATION_DATA}\n
    {FLAGS}\n
    {XXX_NEW_ATTRIBUTE}\n
    {NONCE_B64}\n
    {SIGNING_KEY_TYPE}
    ```
- Rules for data fields and templates:
  - All unsupported data fields are treated as `T{TEXT}` (e.g. arbitrary attribute with text as it is)
  - All unsupported templates are treated as [`0` Generic](#0-generic)

## How to generate QR codes

The general principles of using offline signatures in PowerAuth are documented in chapter [Offline Signatures](https://github.com/wultra/powerauth-server/blob/develop/docs/Offline-Signatures.md).

The concrete steps for generating offline signature QR codes using PowerAuth SOAP service for Web Flow are following:

### 1. Construct offline signature data payload:

```
{OPERATION_ID}\n
{TITLE}\n
{MESSAGE}\n
{OPERATION_DATA}\n
{FLAGS}
```

For example:
```
5ff1b1ed-a3cc-45a3-8ab0-ed60950312b6
Payment
Please confirm this payment
A1*A100CZK*ICZ2730300000001165254011*D20180425
B
```

The meaning of individual fields is explained in chapter [Operation Attributes](#operation-attributes). Note that the field values should be normalized as discussed in the same chapter.

### 2. Call PowerAuth SOAP method 'create personalized offline signature payload'

The SOAP method `createPersonalizedOfflineSignaturePayload` requires two parameters:
- `activationId` - ID of the activation of mobile device
- `data` - data constructed in step 1

The SOAP method is documented in [PowerAuth documentation](https://github.com/wultra/powerauth-server/blob/develop/docs/SOAP-Service-Methods.md#method-createpersonalizedofflinesignaturepayload).

### 3. Obtain response from 'create personalized offline signature payload' SOAP method and validate QR code data

The response from SOAP method `createPersonalizedOfflineSignaturePayload` contains data required to display the QR code in field `offlineData`.

The format of `offlineData` is following:
```
{DATA}\n{NONCE_B64}\n{KEY_SERVER_PRIVATE_INDICATOR}{ECDSA_SIGNATURE}
```

The `nonce` field is available separately in response, so that the `nonce` can be used for signature verification as documented in [Offline Signatures](https://github.com/wultra/powerauth-server/blob/develop/docs/Offline-Signatures.md#verifying-offline-signatures).

### 4. Generate QR code

Generate the QR code from `offlineData`. Code example in Java:
```java
            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(offlineData.getBytes("UTF-8"), "ISO-8859-1"),
                    BarcodeFormat.QR_CODE,
                    size,
                    size);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + BaseEncoding.base64().encode(bytes);
```

### 5. Verify ECDSA signature of offlineData

The `ECDSA_SIGNATURE` should be validated on mobile device to verify authenticity of received data by taking contents of `offlineData` before the `ECDSA_SIGNATURE` and computing the ECDSA signature using `KEY_SERVER_PRIVATE`. Both signatures must match before continuing with the offline data signature verification.
