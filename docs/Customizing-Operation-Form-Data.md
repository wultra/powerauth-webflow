# Customizing Operation Form Data

When creating an operation, you can customize the operation form data. This customization has an effect on how the operation form data is displayed during the operation review step.

The customization should be done by the client which initiates the operation before the authentication process is initiated.

For more information about operation form data see the [Next Step REST API](./Next-Step-Server-REST-API-Reference.md#operation-formdata).

## Standard Operation Form Data Attributes

Following attributes are required to be specified for each operation:
- `title` - title of the operation
- `greeting` - message for the user related to the operation displayed in web interface
- `summary` - summary of the operation shown in push messages

## Custom Operation Form Data Attributes

Following structured custom form data attributes are available:
- `AMOUNT` - amount with currency
- `NOTE` - free text
- `BANK_ACCOUNT_CHOICE` - choice of a bank account
- `KEY_VALUE` - generic key-value field
- `BANNER` - banner which is displayed above form field
- `HEADING` - heading with label 
- `PARTY_INFO` - information about party

## Value Formatting

Following form data attributes support value formatting:
- `AMOUNT`
- `NOTE`
- `KEY_VALUE`
- `HEADING`

The value is formatted based on specified format type. The following format types can be used:
- `TEXT` - value is not formatted
- `LOCALIZED_TEXT` - value is localized using localization key from message resources
- `DATE` - value is formatted as date using current locale, expected value format is YYYY-MM-DD
- `NUMBER` - value is formatted as number using current locale
- `AMOUNT` - value is formatted as amount with currency using current locale
- `ACCOUNT` - value is not formatted (reserved for future use)

## Resource Localization

Form data labels are specified using localization key, such as "operation.title". This key is localized using resources. See chapter [Customizing Web Flow Appearance](https://github.com/wultra/powerauth-webflow-customization/wiki/Customizing-Web-Flow-Appearance) for details about updating resources.

Form data value localization for custom attributes:
- Form data values are localized in case they use the `LOCALIZED_TEXT` value format type.
- Currency in `AMOUNT` form data type is localized using message resource: `currency.[currency].name`.

Other form data values are not localized and are displayed as received.

## Resource Translation

Resource translation is process of inserting other form data values into existing values.

Example:
```
operation.summary=Hello, please confirm payment {operation.amount} {operation.currency} to account {operation.account}.
```

Summary is translated into:
```
operation.summary=Hello, please confirm payment 100 CZK to account 12345678/0123.
```

Resource translation is performed on following attribute values:
- `title`
- `greeting`
- `summary`

## Java API for Operation Form Data

See example below:
```java

// variable definition
String account = "238400856/0300";
BigDecimal amount = BigDecimal.valueOf(100);
String currency = "CZK";
String note = "Utility Bill Payment - 05/2017";
String dueDate = "2017-06-29";

// operation form data initialization
OperationFormData formData = new OperationFormData();
formData.addTitle("operation.title");
formData.addGreeting("operation.greeting");
formData.addSummary("operation.summary");
formData.addAmount("operation.amount", amount, "operation.currency", currency);
formData.addKeyValue("operation.account", account, ValueFormatType.ACCOUNT);
formData.addKeyValue("operation.dueDate", dueDate, ValueFormatType.DATE);
formData.addNote("operation.note", note, ValueFormatType.TEXT);

// sample operation configuration for bank account choice select
OperationFormFieldConfig bankAccountConfig = new OperationFormFieldConfig();
bankAccountConfig.setId("operation.bankAccountChoice");
bankAccountConfig.setEnabled(false);
bankAccountConfig.setDefaultValue("CZ4043210000000087654321");
formData.getConfig().add(bankAccountConfig);

// operation initialization
final ObjectResponse<CreateOperationResponse> payment = client.createOperation("authorize_payment", data, formData, null);
session.setAttribute("operationId", payment.getResponseObject().getOperationId());
```

## Decorating Operation Form Data in Data Adapter

Operation form data can be decorated in Data Adapter by implementing method `decorateFormData`. The additional form attributes appear during the operation review step (after the user was authenticated and operation authorization is required).

### Adding bank account choice

The example below decorates operation form data -- three bank accounts are added to the `BANK_ACCOUNT_CHOICE` attribute:
```java
List<BankAccount> bankAccounts = new ArrayList<>();

BankAccount bankAccount1 = new BankAccount();
bankAccount1.setName("Běžný účet v CZK");
bankAccount1.setBalance(new BigDecimal("24394.52"));
bankAccount1.setNumber("12345678/1234");
bankAccount1.setAccountId("CZ4012340000000012345678");
bankAccount1.setCurrency("CZK");
bankAccounts.add(bankAccount1);

BankAccount bankAccount2 = new BankAccount();
bankAccount2.setName("Spořící účet v CZK");
bankAccount2.setBalance(new BigDecimal("158121.10"));
bankAccount2.setNumber("87654321/4321");
bankAccount2.setAccountId("CZ4043210000000087654321");
bankAccount2.setCurrency("CZK");
bankAccounts.add(bankAccount2);

BankAccount bankAccount3 = new BankAccount();
bankAccount3.setName("Spořící účet v EUR");
bankAccount3.setBalance(new BigDecimal("1.90"));
bankAccount3.setNumber("44444444/1111");
bankAccount3.setAccountId("CZ4011110000000044444444");
bankAccount3.setCurrency("EUR");
bankAccount3.setUsableForPayment(false);
bankAccount3.setUnusableForPaymentReason(dataAdapterI18NService.messageSource().getMessage("operationReview.balanceTooLow", null, LocaleContextHolder.getLocale()));
bankAccounts.add(bankAccount3);

boolean choiceEnabled = true;
String defaultValue = "CZ4012340000000012345678";

List<FormFieldConfig> configs = formData.getConfig();
for (FormFieldConfig config: configs) {
    if ("operation.bankAccountChoice".equals(config.getId())) {
        choiceEnabled = config.isEnabled();
        // You should check the default value against list of available accounts.
        defaultValue = config.getDefaultValue();
    }
}
Attribute attr = formData.addBankAccountChoice(BANK_ACCOUNT_CHOICE_ID, bankAccounts, choiceEnabled, defaultValue);
```

### Adding form banners

The example below adds a banner on top of the operation form:
```java
formData.addBanner(BannerType.BANNER_WARNING, "banner.warning");
```

The available banner types are:
- BANNER_ERROR
- BANNER_WARNING
- BANNER_INFO

The second parameter is the localization string with text message of the banner.

### Adding banners before fields

The example below decorates operation form data by adding banner above the `BANK_ACCOUNT_CHOICE` attribute which was previously added:
```java
formData.addBannerBeforeField(BannerType.BANNER_WARNING, "banner.invalidAccount", attr);
```

The available banner types are:
- BANNER_ERROR
- BANNER_WARNING
- BANNER_INFO

The second parameter is the localization string with text message of the banner.

The third parameter is the attribute before which the banner is added.

### Adding headings
The example below shows how to add a heading attribute:

```java
formData.addHeading("operation.heading1", "operation.headingTop", ValueFormatType.LOCALIZED_TEXT);
```

The parameters are:
* ID of the heading attribute (label is not displayed, so it is not localized)
* Localization ID of message for heading
* LOCALIZED_TEXT is the value format type for text localization by chosen language

For non-localized version of heading you can use:
```java
formData.addHeading("operation.heading1", "Payment");
```

### Adding party information
The example below shows how to add information about a third party:
```java
// Add information about 3rd party
PartyInfo partyInfo = new PartyInfo();
partyInfo.setName("Tesco PLC");
partyInfo.setLogoUrl("https://www.tescoplc.com/media/474818/plc_image_logo.png?anchor=center&mode=crop&width=820&height=462&rnd=131722809190000000");
partyInfo.setDescription("British groceries and general merchandise retailer");
partyInfo.setWebsiteUrl("https://www.tescoplc.com");
formData.addPartyInfo("operation.partyInfo", partyInfo);
```

In order to add party information before an existing field instead of appending it after last field, use the `addPartyInfoBeforeField()` method.

### Operation Form Data JSON

When creating operations using Next Step API, you can specify operation form data using JSON instead of using the Java API.

Note: `label` and `formattedValue` fields in examples below are always null, because these values are used internally:
- `label` is localized by taking the `id` and localizing it into current language
- `formattedValue` is constructed using logic based on `valueFormatType` and field value

`AMOUNT`:
```json
        {
          "type": "AMOUNT",
          "id": "operation.amount",
          "label": null,
          "valueFormatType": "AMOUNT",
          "formattedValue": null,
          "amount": 100,
          "currency": "CZK",
          "currencyId": "operation.currency"
        }
```

Remarks:
- Always use `AMOUNT` as `valueFormatType`
- The `amount` value can use decimal point
- Use ISO format of `currency`, the value is localized using message resources (e.g. `currency.CZK.name`)
- The `currencyId` value is used to determine localization ID for the word "currency"

`KEY_VALUE`:
```json
        {
          "type": "KEY_VALUE",
          "id": "operation.account",
          "label": null,
          "valueFormatType": "ACCOUNT",
          "formattedValue": null,
          "value": "238400856/0300"
        }
```
Remarks:
- Supported value format types which influence the `formattedValue`: 
  - `TEXT` - non-localized text (as is)
  - `LOCALIZED_TEXT` - localized text (using message resources)
  - `DATE` - date formatted in operation locale
  - `NUMBER` - generic number formatted in operation locale
  - `AMOUNT` - monetary amount formatted in operation locale
  - `ACCOUNT` - account value (not formatted because the syntax may differ greatly)

`NOTE`:
```json
        {
          "type": "NOTE",
          "id": "operation.note",
          "label": null,
          "valueFormatType": "TEXT",
          "formattedValue": null,
          "note": "Utility Bill Payment - 05/2017"
        }
```

Remarks:
- The `note` string is not localized, it is taken "as is".

`HEADING`:
```json
        {
          "type": "HEADING",
          "id": "operation.heading1",
          "label": null,
          "valueFormatType": "TEXT",
          "formattedValue": null,
          "value": "Heading"
        }
```

Remarks:
- The `label` is ignored, the `HEADING` field uses only a value.
- The `value` is formatted using given `valueFormatType` (same value format types as in `KEY_VALUE`).

`BANNER`:
```json
        {
          "type": "BANNER",
          "id": "banner.error",
          "label": null,
          "message": null,
          "bannerType": "BANNER_ERROR"
        }
```

Remarks:
- The `label` is ignored, the `BANNER` field uses only a value.
- The banner message is taken from the `id` field by localizing message resource with such ID.
- Supported banner types:
   - BANNER_ERROR
   - BANNER_WARNING
   - BANNER_INFO

`PARTY_INFO`:
```json
        {
          "type": "PARTY_INFO",
          "id": "operation.partyInfo",
          "label": null,
          "partyInfo": {
            "logoUrl": "https://itesco.cz/img/logo/logo.svg",
            "name": "Tesco",
            "description": "Objevte více příběhů psaných s chutí",
            "websiteUrl": "https://itesco.cz/hello/vse-o-jidle/pribehy-psane-s-chuti/clanek/tomovy-burgery-pro-zapalene-fanousky/15012"
          }
        }
```

Remarks:
- The value is structured and it is not localized.

## Pending issues for operation form data:
- https://github.com/wultra/powerauth-webflow/issues/389
- https://github.com/wultra/powerauth-webflow/issues/221
