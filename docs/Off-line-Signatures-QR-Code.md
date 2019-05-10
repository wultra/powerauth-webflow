# Off-line Signature QR Code

## Table of Contents

- [Introduction](#introduction)
- [Operation Attributes](#operation-attributes)
- [Operation Data](#operation-data)
- [Flags](#flags)
- [Signature Calculation](#signature)
- [Forward Compatibility](#forward-compatibility)
- [Offline Signature Process Description](#offline-signature-process-description)

## Introduction

This chapter describes how operation data is encoded into QR code for the purpose of off-line verification.


## Operation Attributes

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
7. `{SIGNING_KEY_TYPE}{ECDSA_QRDATA_SIGNATURE_BASE64}` - ECDSA signature in Base64 format. The signature string has an one prefix character, containing an information about signing key type.

Then, the final string for QR code is simple, new-line separated list of attributes:

```
{OPERATION_ID}\n
{TITLE}\n
{MESSAGE}\n
{OPERATION_DATA}\n
{FLAGS}\n
{NONCE_B64}\n
{SIGNING_KEY_TYPE}{ECDSA_QRDATA_SIGNATURE_BASE64}
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

## Operation Data

Operation data is an asterisk separated list of fields, where the first field defines a version of operation data and template. Other fields are additional and contains typically significant attributes, which has to be presented to the user, before the operation is confirmed and signed.

An exact details of the operation data structure can be found in the separate documentation:

- [Operation Data Structure](./Operation-Data.md)

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
- `{ECDSA_QRDATA_SIGNATURE_BASE64}` is ECDSA signature calculated with selected private key.

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

## Forward Compatibility

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
    {SIGNING_KEY_TYPE}{ECDSA_QRDATA_SIGNATURE_BASE64}
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
  - All unsupported templates are treated as [`0` Generic](./Operation-Data.md#0-generic)

## Offline Signature Process Description

### 1. Generate QR Codes

The general principles of using offline signatures in PowerAuth are documented in chapter [Offline Signatures](https://github.com/wultra/powerauth-server/blob/develop/docs/Offline-Signatures.md).

The concrete steps for generating offline signature QR codes using PowerAuth SOAP service for Web Flow are following:

#### 1.1. Construct offline signature data payload:

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

#### 1.2. Fetch Data For Offline Signatures

Call PowerAuth SOAP method to create personalized offline signature payload. The SOAP method `createPersonalizedOfflineSignaturePayload` requires two parameters:

- `activationId` - ID of the activation of mobile device
- `data` - data constructed in step 1

The SOAP method is documented in [PowerAuth documentation](https://github.com/wultra/powerauth-server/blob/develop/docs/SOAP-Service-Methods.md#method-createpersonalizedofflinesignaturepayload).

The response from SOAP method `createPersonalizedOfflineSignaturePayload` contains:

- Data required to display the QR code in field `offlineData`
- Random cryptographic nonce.

The format of `offlineData` is following:
```
{DATA}\n{NONCE_B64}\n{KEY_SERVER_PRIVATE_INDICATOR}{ECDSA_SIGNATURE}
```

The `nonce` field is available separately in response, so that the `nonce` can be used for signature verification as documented in [Offline Signatures](https://github.com/wultra/powerauth-server/blob/develop/docs/Offline-Signatures.md#verifying-offline-signatures).

#### 1.3. Display Data To The User

To display correct information in the web browser, generate the QR code from `offlineData`.

Code example in Java:

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

Display this QR code to the user so that it can be scanned via the mobile app.

The value of `nonce` must be stored on a browser level, either as a hidden HTML form input field or as v React.js variable, or alternatively, it can also be stored in the user session. `Nonce` value is required for later offline signature validation.

### 2. Computing Signatures on Mobile Device

#### 2.1. Verify ECDSA Signature of Offline Data

After user scans the QR code using a mobile app, the `ECDSA_SIGNATURE` should be validated on mobile device to verify authenticity of received data by taking contents of `offlineData` before the `ECDSA_SIGNATURE` and computing the ECDSA signature using `KEY_SERVER_PRIVATE`. Both signatures must match before continuing with the offline data signature verification.

#### 2.2. Computing the Signature

Mobile device prompts the user for the PIN code or use of a biometry and computes 4x4 digit long authentication code.

### 3. Validating the Signature

#### 3.1. Processing User Input

After user enters 4x4 digits, the value must be converted to standard PowerAuth signature format that uses 2x8 digits.

#### 3.2. Preparing Signature Base String

Now, you need to prepare a normalized data package. For this, you need `data` (as obtained in 1.1.), `nonce` value (as obtained in 1.2) andn two constants: `POST` and `/operation/authorize/offline`.

```java
String signatureBaseString
    = PowerAuthHttpBody.getSignatureBaseString(
        "POST",
        "/operation/authorize/offline",
        BaseEncoding.base64().decode(nonce),
        data.getBytes()
);
```

#### 3.4. Verifying Signature

To verify signature, you need to call the SOAP method [`verifyOfflineSignature`](https://developers.wultra.com/docs/develop/powerauth-server/SOAP-Service-Methods#method-verifyofflinesignature) providing:

- `signatureBaseString` (as obtained in 3.2.) - as data for verification
- `signature` - value of the signature entered by the user (as obtained in 3.1., 2x8 digits)
- `activationId` - identifier of the activation (to know which device is responsible for verification)
- `signatureType` - type of the signature (`POSSESSION_KNOWLEDGE`).

The method returns information about signature verification, see the SOAP method documentation for details.
