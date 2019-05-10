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

The general principles of using offline signatures in PowerAuth are documented in chapter [Offline Signatures](https://github.com/wultra/powerauth-server/blob/develop/docs/Offline-Signatures.md). This chapter provides an more detailed description of the step-by-step process.

There are three stages of offline signature verification:

1) Generate QR code and display it to the user.
2) User uses mobile app to scan the QR code and compute offline signature.
3) Verify the offline signature.

### 1. Generate QR Codes

The first step of the process is to generate a QR code to be displayed to the user.

#### 1.1. Construct offline signature data payload:

First, you need to prepare a QR code data stup from the information you already should know - operation ID, operation title and description, operation data and additional flags:

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

Now, you need to prepare the data for the QR code display. Call the PowerAuth SOAP method to create a personalized offline signature payload. The SOAP method `createPersonalizedOfflineSignaturePayload` requires two parameters:

- `activationId` - ID of the activation of mobile device
- `data` - data constructed in step 1

The SOAP method is documented in the [PowerAuth documentation](https://github.com/wultra/powerauth-server/blob/develop/docs/SOAP-Service-Methods.md#method-createpersonalizedofflinesignaturepayload).

In the response from the SOAP method `createPersonalizedOfflineSignaturePayload`, you will receive:

- `offlineData` - The exact data to be displayed inside the QR code.
- `nonce` - A random cryptographic nonce.

The `nonce` field is available separately in response, so that it can be used for signature verification later, as documented in [Offline Signatures](https://github.com/wultra/powerauth-server/blob/develop/docs/Offline-Signatures.md#verifying-offline-signatures).

Note: The format of the `offlineData` is the following:

```
{DATA}\n{NONCE_B64}\n{KEY_SERVER_PRIVATE_INDICATOR}{ECDSA_QRDATA_SIGNATURE_BASE64}
```

As you can see, the `offlineData` already contain `nonce` value (in Base64 format) since the mobile app needs to scan the `nonce` value to compute the signature. However, the SOAP service still returns the value separately - since `nonce` must be used later on the back-end side, we wanted to avoid the necessity to parse the `offlineData` and hence we return `nonce` as a standalone response attribute.

#### 1.3. Display Data To The User

To display QR code in the web browser, generate the QR code from `offlineData` you obtained in 1.2 (no changes to the data are needed).

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

The user can now scan the QR code via the mobile token app.

The value of `nonce` must be stored somewhere - for example on a browser level, either as a hidden HTML form input field or as a JavaScript variable, or alternatively, it can be stored in the user session by the specific operation. The `nonce` value is required for later offline signature validation, see step 3.

### 2. Computing Signatures on Mobile Device

#### 2.1. Verify ECDSA Signature of Offline Data

After user scans the QR code using a mobile app, the `ECDSA_QRDATA_SIGNATURE_BASE64` should be validated on mobile device to verify authenticity of received data by taking contents of `offlineData` before the `ECDSA_QRDATA_SIGNATURE_BASE64` and computing the ECDSA signature using `KEY_SERVER_PRIVATE`. Both signatures must match before continuing with the offline data signature verification.

#### 2.2. Computing the Signature

Mobile device prompts the user for the PIN code or use of a biometry and computes 4x4 digit long authentication code to be rewritten manually.

### 3. Validating the Signature

#### 3.1. Processing The User Input

After user enters 4x4 digits in the browser, the value must be converted into a standard PowerAuth signature format that uses 2x8 digits. For example, an offline signature `1234-5678-9012-3456` needs to be converted into `12345678-90123456` (removing 1st and 3rd dash).

#### 3.2. Preparing Signature Base String

Now, you need to prepare a normalized data package called "signature base string". This is the payload that mobile app used to compute the signature - you need to have the same signature base string in order to be able to verify the signature.

To compute the signature base string, you need:

- `data` (as obtained in 1.1.)
- `nonce` value (as obtained in 1.2)
- two static constants: `POST` and `/operation/authorize/offline`

The [algorithm for signature data normalization](https://developers.wultra.com/docs/develop/powerauth-crypto/Computing-and-Validating-Signatures#normalized-data-for-http-requests) is available in the cryptography description.

The Java class [PowerAuthHttpBody](https://github.com/wultra/powerauth-crypto/blob/master/powerauth-java-http/src/main/java/io/getlime/security/powerauth/http/PowerAuthHttpBody.java) already contains a ready to use method for computing the normalized signature base string:

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

- `activationId` - identifier of the activation (to know which device is responsible for verification)
- `data` (represented by `signatureBaseString` as obtained in 3.2.) - as data for verification
- `signature` - value of the signature entered by the user (as obtained in 3.1., 2x8 digits)
- `signatureType` - type of the signature (`POSSESSION_KNOWLEDGE`).

The method returns information about signature verification:

- `signatureValid` You can use this value to determine if the signature verification was successful or not.
- `activationStatus` - Activation status after this attempt of the signature validation.
- `blockedReason` - In case the activation is blocked, this attribute contains additional info about the reason.
- `activationId` - Activation ID used for validating the signature.
- `userId` - User ID associated with the activation who authenticated to compute the signature.
- `applicationId` - Application ID of the application that is associated with given activation ID and was used to compute the signature.
- `signatureType` - Signature type that was used to compute the signature value.
- `remainingAttempts` - How many attempts are remaining for the signature validation (single, activation related counter).

See the SOAP method documentation for details.
