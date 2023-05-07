# Migration from 0.19.0 to 0.20.0

## Changes Since 0.19.0

### Generalized method for decorating form data in Data Adapter

In previous releases, Data Adater interface contained method:

```java
BankAccountListResponse fetchBankAccounts(String userId, String operationName, String operationId, OperationFormData formData)
```

This method has been replaced by:
```java
DecorateOperationFormDataResponse decorateFormData(String userId, OperationContext operationContext)
```

Additionally, almost all methods in Data Adapter interface contain the `OperationContext` object which provides access to:
* Operation ID
* Operation name
* Operation data
* Operation form data
* Additional operation attributes

The updated Data Adapter interface and REST API is documented in following chapters:
* [Data Adapter REST API Reference](./Data-Adapter-REST-API-Reference.md)
* [Implementing the Data Adapter Interface](https://github.com/wultra/powerauth-webflow-customization/tree/develop/docs/Implementing-the-Data-Adapter-Interface.md)

### DB Migration to BCrypt

With the new version of PowerAuth Web Flow, plain-text credentials are no longer allowed. As a result, OAuth 2.1 client credentials need to be migrated to use `bcrypt` algorithm.

To migrate existing database, first generate `bcrypt` values of existing passwords, for example like so:

```sh
$ htpasswd -bnBC 12 "" changeme | tr -d ':\n' | sed 's/$2y/$2a/'
$2a$12$XNPTj1HKxC4ORnKYo1gUkOybWYydJkCh9jbjQ1lBKgXIH0U6mTZe2
```

Of course, you can use any means for generating `bcrypt` values (maybe except for untrusted online generators, for obvious reasons) and any sufficient value of `bcrypt` cost (we use `12` in the example above).

After that, you need to update database record for given OAuth 2.1 client instance, in our case identified by `client_id` value:

```sql
UPDATE oauth_client_details SET client_secret='$2a$12$kJQvZfvet52pFIwyxUjbOev4kok7P07nc..cs2FzYBEgcIi.w2mjC' WHERE client_id='democlient'
```

### Separation of Data Adapter model

Data Adapter now uses it's own model class for FormData and related model classes to avoid dependency on Next Step model.

See: https://github.com/wultra/powerauth-webflow/blob/develop/powerauth-data-adapter-model/src/main/java/io/getlime/security/powerauth/lib/dataadapter/model/entity/FormData.java

### Migration of Data Adapter project

Sample Data Adapter implementation has been moved to: https://github.com/wultra/powerauth-webflow-customization

The Data Adapter source code is no longer part of the powerauth-webflow repository except for Data Adapter model and Data Adapter client projects.

### Support for banners

The new `BANNER` form data type can be added to an operation. When creating a new operation you can specify banners which are displayed as messages with an icon above the operation.

Examples:
```
formData.addBanner(BannerType.BANNER_ERROR, "banner.error");
formData.addBanner(BannerType.BANNER_WARNING, "banner.warning");
formData.addBanner(BannerType.BANNER_INFO, "banner.info");
```

The messages are localized using message resources based on provided localization keys.

Banners can be also inserted above any form data fields (usually used when decorating operation form data):

```java
Attribute attr = formData.addBankAccountChoice(BANK_ACCOUNT_CHOICE_ID, bankAccounts, choiceEnabled, defaultValue);
formData.addBannerBeforeField(BannerType.BANNER_WARNING, "banner.invalidAccount", attr);
```

### Simplified interface for form data field value formatting

The `ValueFormatType` enumeration was moved to a standalone enum file to simplify the API:

Example:
```java
formData.addKeyValue("operation.dueDate", paymentForm.getDueDate(), ValueFormatType.DATE);
```

### Added "heading" form data type

The new `HEADING` form data type can be added to an operation.

Example:
```java
formData.addHeading("operation.heading", "Something important", ValueFormatType.TEXT);
```

### Added "party info" form data type displaying third party information

The new `PARTY_INFO` data type can be added to an operation.

Example:
```java
// Add information about 3rd party
PartyInfo partyInfo = new PartyInfo();
partyInfo.setName("Tesco PLC");
partyInfo.setLogoUrl("https://www.tescoplc.com/media/474818/plc_image_logo.png?anchor=center&mode=crop&width=820&height=462&rnd=131722809190000000");
partyInfo.setDescription("British groceries and general merchandise retailer");
partyInfo.setWebsiteUrl("https://www.tescoplc.com");
formData.addPartyInfo("operation.partyInfo", partyInfo);
```

### Updated offline QR codes and operation templates

Operation templates were introduced for better display of operation details during offline signature verification. The changes are documented in [Off line Signatures QR Code](https://github.com/wultra/powerauth-webflow/wiki/Off-line-Signatures-QR-Code).

A new DB table `ns_operation_config` was added for configuration of operation templates.

MySQL:
```sql
-- Table ns_operation_config stores configuration of operations.
-- Each operation type (defined by operation_name) has a related mobile token template and configuration.
CREATE TABLE ns_operation_config (
  operation_name            VARCHAR(32) PRIMARY KEY,
  template_version          CHAR,
  template_id               INTEGER,
  mobile_token_mode         VARCHAR(256)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Oracle:
```sql
-- Table ns_operation_config stores configuration of operations.
-- Each operation type (defined by operation_name) has a related mobile token template and configuration of signatures.
CREATE TABLE ns_operation_config (
  operation_name            VARCHAR(32) PRIMARY KEY,
  template_version          VARCHAR(1),
  template_id               INTEGER,
  mobile_token_mode         VARCHAR(256)
);
```

In case you create the table by a different user than owner of the PowerAuth schema, grant access to this table (change `powerauth` to actual DB schema name in case it differs in your deployment):
```sql
GRANT ALL PRIVILEGES ON powerauth.ns_operation_config TO powerauth;
```

Existing operations need to be configured, see example:
```sql
INSERT INTO ns_operation_config (operation_name, template_version, template_id, mobile_token_mode) VALUES ('login', 'A', 2, '{"type":"2FA","variants":["possession_knowledge","possession_biometry"]}');
INSERT INTO ns_operation_config (operation_name, template_version, template_id, mobile_token_mode) VALUES ('authorize_payment', 'A', 1, '{"type":"2FA","variants":["possession_knowledge","possession_biometry"]}');
```

The parameters are:
* `operation_name` - has same meaning as in table `ns_step_definition`, it identifies an operation
* `template_version` - use `A` as the current version
* `template_id` - 1 = login, 2 = payment
* `mobile_token_mode` - type field contains either `1FA` or `2FA` depending on number of factors, variants contains allowed keys. See [PowerAuth documentation](https://github.com/wultra/powerauth-crypto/wiki/Computing-and-Validating-Signatures) for details.

## Migrate to new format of operation data

Operation data format has been standardized in Web Flow. You can use class `OperationDataBuilder` to generate operation data easily. The class is located in project `powerauth-nextstep-model`. 

Sample usage:

```java
        String operationData = new OperationDataBuilder()
                .templateVersion("A")
                .templateId("1")
                .attr1().amount(payment.getAmount(), payment.getCurrency())
                .attr2().accountGeneric(payment.getAccount())
                .attr3().reference(payment.getReference())
                .attr4().date(payment.getDueDate())
                .attr5().note(payment.getNote())
                .build();
```

You can omit attributes when they are not available (e.g. payment reference).

For more details, see: [Operation Data Syntax](./Off-line-Signatures-QR-Code.md#operation-data).

## Java 9 support

Web Flow version 0.20.0 supports Java 9. However, due to the short support cycle of Java 9 we recommend to use Java 8 in production for PowerAuth and wait with Java upgrade for Java 11 which will be the next long-term support release.

## JMX disabled by default

Spring JMX (Java Management Extensions) is now disabled by default. This change slightly decreases startup time of PowerAuth and avoids unecessary exposing of information about PowerAuth data sources. 

If you want to enable Spring JMX, you can re-enable it using configuration property:

```properties
spring.jmx.enabled=true
```

## Upgrade to Spring boot 2

The whole PowerAuth stack now uses Spring boot 2. In case you integrate your application with PowerAuth using client APIs we recommend to migrate your application to Spring boot 2 to avoid compatiblity issues.

## Improved logging of PowerAuth

The whole PowerAuth stack now logs additional information on INFO log level. Depending on number of requests from clients the log files can increase in size. 

In case you run into disk space issues due to log size, we recommend you take one of the following actions:
* Configure log rotation in web container which hosts PowerAuth.
* Change the default log level to `WARN` using configuration property:
```properties
logging.level.root=WARN
```
* Allocate more disk space for logs.