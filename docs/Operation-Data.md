# Operation Data Structure

## Table of Contents

- [Operation Data Overview](#operation-data-overview)
    - [Header](#header)
    - [Known Template Types](#known-template-types)
    - [Data Types](#data-types)
    - [Data Fields](#data-fields)
- [Template Details](#template-details)
    - [`0` Generic](#0-generic)
    - [`1` Payment](#1-payment)
    - [`2` Login Request](#2-login-request)

## Operation Data Overview

Operation data is an asterisk separated list of fields, where the first field defines a version of operation data and template. Other fields are additional and typically contain significant attributes, which has to be presented to the user, before the operation is confirmed and signed.

Each field has its unique type, defined by first letter. For example, following string contains header `A1` and three additional fields: "Amount", "Account" and "Date":

```
A1*A100CZK*ICZ2730300000001165254011*D20180425
```

### Header

Header is composed from two parts:

- `{VERSION}{TEMPLATE}`, where
    - `{VERSION}` is an one capital letter defining version of operation data. First version is `A`, then `B`, `C`, etc...
    - `{TEMPLATE}` is a decimal number defining a template which helps with other fields interpretation.

Version `A` has following limitations:

- Templates from `0` up to `99` are supported
- Up to 5 additional fields are supported

### Known Template Types

- `0` - Generic template. Each field will be displayed with "default" title.
- `1` - Payment data
- `2` - Login confirmation

If template defines an optional field and this field is not present in operation data, then an empty string can be used. For example, following strings defines various forms of payment:

- `A1*A100CZK*ICZ2730300000001165254011***` - all optional parameters are empty
- `A1*A100CZK*ICZ2730300000001165254011` - the same as above, but with omitted asterisks
- `A1*A100CZK*ICZ2730300000001165254011***Nnote for recipient` - Last note is optional but used, so asterisks must be used to put note field at the right position.

_Note: Templates other than `0` may have an implicit `{TITLE}` and `{MESSAGE}` attributes displayed on the client side (in the mobile application). For example, it's not required to issue "Payment" and "Please confirm this payment" titles, when the "payment" template is used, simple an empty newline can be used for both data attributes. This rule unfortunately goes agains our forward compatibility principle, so it's recommended only for version `A` templates. Unless data capacity is a critical issue, we recommend using explicit title and message._

### Data Types

This section defines data types available for data fields:

- `{DECIMAL}` - a decimal number with dot as decimal separator. The Examples:
    - `100.10` - number with a fractional part
    - `1492` - number without a fractional part

- `{CURRENCY}` - Currency code from [ISO 4217](https://en.wikipedia.org/wiki/ISO_4217)

- `{IBAN}` - Fully qualified IBAN with optional, comma separated BIC. For example:
    - `CZ2730300000001165254011` - IBAN without BIC code
    - `CZ2730300000001165254011,AIRACZPP` - IBAN with BIC code

- `{TEXT}` - UTF-8 encoded text with few escaped characters:
    - `\n` - newline
    - `\\` - escape for backslash
    - `\*` - escape for asterisk
    - All characters with ASCII code < 32 are forbidden (e.g. `\t` should not be in the string)
    - In the QR code generator, it is recommended to compress spaces. That means that two and more consequent space characters should be replaced with just one space character.

### Data Fields

- `A{DECIMAL}{CURRENCY}` - **Amount** with currency.
    - `A100CZK`
    - `A1492.50EUR`
    - **"Amount"** is a default title, when template is not recognized.

- `I{IBAN}` - **Counter account** in IBAN format, with optional BIC code:
    - `ICZ2730300000001165254011`
    - `ICZ2730300000001165254011,AIRACZPP`
    - **"Account"** is a default title, when template is not recognized.

- `Q{TEXT}` - **Counter account** in arbitrary format:
    - `Q1165254011/3030` - an example for czech account
    - **"Account"** is a default title, when template is not recognized.

- `D{DATE}` - **date field** in `YYYYMMDD` format. For example:
    - `D20180425`
    - **"Date"** is a default title

- `R{TEXT}` - **Operation's reference**. This field contains a general reference associated with the operation. For example, for payment it can be a payment reference or in case of czech domestic payment, for trasmission of symbols asscociated with payment:
    - `RID3343432434` - some payment reference
    - `R/VS123456/SS345/KS` - Czech specific, defines: VS=123456, SS=345, KS=empty
    - **"Reference"** is a default title

- `N{TEXT}` - **Note** associated with the operation. It can be for example note associated with the payment.
    - `NZa vecerne pivo`
    - **"Note"** is a default title

- `T{TEXT}` - **Arbitrary textual** field, displayed as is, without any additional processing.
    - `TRate 1EUR = 25,49CZK`
    - **Attribute N**, is a default title, where N is an auto incremented number, starting with 1. For example, if you use two `T` fields in data, then the first in row will have "Attribute 1" and second "Attributer 2" title.

## Template Details

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

Example operation data:

```
A1*A100CZK*ICZ2730300000001165254011*R/VS123456/SS/KS*D20180425
```

### `2` Login Request

*Available since version `A`*

| Attribute | Title                        |
|-----------|:-----------------------------|
| Title     | Login request                |
| Message   | Please confirm login into *internet banking.* |

Data fields for this type of teplate are not specified, so any available fields will be interpreted as for generic template.

Example operation data:

```
A2
```
