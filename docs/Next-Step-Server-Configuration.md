# Configuring Next Step

This chapter describes the post-install configuration of Next Step Server. Use it after the server is deployed, the database schema is prepared, and the runtime properties are configured.

During Next Step deployment organizations need to be customized. In case Next Step is used to store user identities, additional configuration of credential and one time password policies and definitions is required. The customization is usually done using SQL, however a RESTful API is also available for each of the configuration steps. 

## Before You Start

Before continuing with the configuration below, make sure that:
- Next Step Server is installed, configured and reachable, see [Next Step Server Installation](./Next-Step-Server-Installation.md)

## Recommended Configuration Order

For a new installation, configure Next Step in the following order:

- organizations
- Next Step applications
- credential and OTP policies
- credential and OTP definitions

If configuration is managed as SQL migrations, keep the same order in your deployment pipeline so that reference data dependencies remain valid.

## Organization Configuration

Next Step requires at least one organization configured. 

Each organization requires following configuration:
- `organization_id` - an identifier of the organization, such as `RETAIL` or `SME`
- `display_name_key` - internationalization key for the organization used in Web Flow when displaying organization name
- `is_default` - whether the organization is the default one, set this flag to `TRUE` only for one organization
- `order_number` - order of the organization in the Web Flow UI, starting by 1
- `default_credential_name` - default credential name is used by Web Flow when performing credential authentication using Next Step
- `default_otp_name` - default OTP name is used by Web Flow when performing OTP authentication using Next Step

The default configuration is following:

```sql
INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number, default_credential_name, default_otp_name) VALUES ('RETAIL', 'organization.retail', 1, 1, 'RETAIL_CREDENTIAL', 'RETAIL_OTP');
```

The default configuration assigns the `RETAIL` organization to all operations. You can define multiple organizations to support
authentication for multiple segments which can have overlapping user IDs, e.g.:

```sql
INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number, default_credential_name, default_otp_name) VALUES ('RETAIL', 'organization.retail', 1, 1, 'RETAIL_CREDENTIAL', 'RETAIL_OTP');
INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number, default_credential_name, default_otp_name) VALUES ('SME', 'organization.sme', 0, 2, 'SME_CREDENTIAL', 'SME_OTP');
```

Such configuration defines two organizations `RETAIL` and `SME`. The user sees two tabs when authenticating with localized labels
based on keys `organization.retail` and `organization.sme`. The user can switch the organization against which the authentication is performed.
The `RETAIL` organization is the default one (it is preselected in the UI). The order of displayed organizations is defined as
`RETAIL`, `SME` using the last parameter.

<!-- begin box warning -->
User ID should represent a natural person. Make sure the user ID used in Next Step Server is consistent in all PowerAuth backends. You can achieve this requirement by assigning unique user IDs in different organizations during user authentication. Alternatively the uniqueness requirement can be achieved by adding a prefix to all user IDs based on the organization against which the user was authenticated (e.g. `RETAIL.12345678`).
<!-- end -->

The organization can be created using REST API by calling the `POST /organization` endpoint.

## Configuration of Next Step Applications

At least one Next Step application must be configured. Each application requires following configuration:
- `application_id` - Next Step application identifier
- `name` - application name
- `description` - description of the application
- `status` - application status: `ACTIVE` or `REMOVED`
- `timestamp_created` - timestamp when application was created

```sql
INSERT INTO ns_application (application_id, name, description, status, timestamp_created) values (1, 'APP', 'Sample application', 'ACTIVE', CURRENT_TIMESTAMP);
```

The application can be created using REST API by calling the `POST /application` endpoint.


## Configuration of Next Step Credential Policies

At least one Next Step credential policy must be configured in case credential authentication is performed. The configuration is not used when Data Adapter proxy is enabled, in this case the configuration record is prepared for potential future switch to Next Step identity once Data Adapter proxy is disabled.

The credential policy requires following configuration:

- `credential_policy_id` - credential policy identifier
- `name` - credential policy name
- `description` - description of the credential policy
- `status` - credential policy status: `ACTIVE` or `REMOVED`
- `username_length_min` - minimum length of the username, use `NULL` value for no limit
- `username_length_max` - maximum length of the username, use `NULL` value for no limit
- `username_allowed_pattern` - regular expression for checking the username pattern, use `NULL` value for no check
- `credential_length_min` - minimum length of the credential, use `NULL` value for no limit
- `credential_length_max` - maximum length of the credential, use `NULL` value for no limit
- `limit_soft` - soft limit for failed authentication attempts using credential (credential status `BLOCKED_TEMPORARY` when limit is exceeded), use `null` value for no limit (e.g. for development purposes)
- `limit_hard` - hard limit for failed authentication attempts using credential (credential status `BLOCKED_PERMANENT` when limit is exceeded), use `null` value for no limit (e.g. for development purposes)
- `check_history_count` - count of historical credential values which should be checked when changing the credential, use `0` for skipping the check
- `rotation_enabled` - whether credential rotation is enabled
- `rotation_days` - number of days for credential rotation, only specify when credential rotation is enabled
- `credential_temp_expiration` - expiration of `TEMPORARY` credentials in seconds
- `username_gen_algorithm` - name of algorithm used for generating username: `NO_USERNAME`, `RANDOM_DIGITS`, or `RANDOM_LETTERS`
- `username_gen_param` - parameters of the username generating algorithm: `length` (only applies to algorithms `RANDOM_DIGITS` and `RANDOM_LETTERS`)
- `credential_gen_algorithm` - name of algorithm used for generating credentials: `RANDOM_PASSWORD` or `RANDOM_PIN`
- `credential_gen_param` - parameters of the credential generating algorithm: `length`, `includeSmallLetters`, `smallLettersCount`, `includeCapitalLetters`, `capitalLettersCount`, `includeDigits`, `digitsCount`, `includeSpecialChars`, `specialCharsCount`
- `credential_val_param` - parameters of credential validation based on the [Passay library rules](https://www.passay.org): `includeWhitespaceRule`, `includeUsernameRule`, `includeAllowedCharacterRule`, `allowedChars`, `includeAllowedRegexRule`, `allowedRegex`, `includeIllegalCharacterRule`, `illegalChars`, `includeIllegalRegexRule`, `illegalRegex`, `includeCharacterRule`, `includeSmallLetters`, `smallLettersMin`, `includeCapitalLetters`, `capitalLettersMin`, `includeAlphabeticalLetters`, `alphabeticalLettersMin`, `includeDigits`, `digitsMin`, `includeSpecialChars`, `specialCharsMin`
- `timestamp_created` - timestamp when credential policy was created

```sql
INSERT INTO ns_credential_policy (credential_policy_id, name, description, status, username_length_min, username_length_max, username_allowed_pattern, credential_length_min, credential_length_max, limit_soft, limit_hard, check_history_count, rotation_enabled, username_gen_algorithm, username_gen_param, credential_gen_algorithm, credential_gen_param, credential_val_param, timestamp_created) values (1, 'CREDENTIAL_POLICY', 'Sample credential policy', 'ACTIVE', 8, 20, '[0-9]+', 8, 40, 3, 5, 3, 0, 'RANDOM_DIGITS', '{"length": 8}', 'RANDOM_PASSWORD', '{"length": 12, "includeSmallLetters": true, "smallLettersCount": 5, "includeCapitalLetters": true, "capitalLettersCount": 5, "includeDigits": true, "digitsCount": 1, "includeSpecialChars": true, "specialCharsCount": 1}', '{"includeWhitespaceRule": true, "includeUsernameRule": true, "includeAllowedCharacterRule": false, "allowedChars": "", "includeAllowedRegexRule": false, "allowedRegex": ".*", "includeIllegalCharacterRule": false, "illegalChars": "", "includeIllegalRegexRule": false, "illegalRegex": "", "includeCharacterRule": true, "includeSmallLetters": "true", "smallLettersMin": 1, "includeCapitalLetters": true, "capitalLettersMin": 1, "includeAlphabeticalLetters": true, "alphabeticalLettersMin": 2, "includeDigits": true, "digitsMin": 1, "includeSpecialChars": true, "specialCharsMin": 1}', CURRENT_TIMESTAMP);
```

The credential policy can be created using REST API by calling the `POST /credential/policy` endpoint.

## Configuration of Next Step OTP Policies

At least one Next Step OTP policy must be configured in case OTP authentication is performed. The configuration is not used when Data Adapter proxy is enabled, in this case the configuration record is prepared for potential future switch to Next Step identity once Data Adapter proxy is disabled.

The OTP policy requires following configuration:
- `otp_policy_id` - OTP policy identifier
- `name` - OTP policy name
- `description` - description of the OTP policy
- `status` - OTP policy status: `ACTIVE` or `REMOVED`
- `length` - length of the OTP authorization code
- `attempt_limit` - limit for failed authentication attempts using OTP (OTP status `BLOCKED` when limit is exceeded), use `0` for no limit
- `expiration_time` - expiration of OTP record
- `gen_algorithm` - name of algorithm used for generating OTP authorization code: `OTP_DATA_DIGEST` or `OTP_RANDOM_DIGIT_GROUPS`
- `gen_param` - parameters of the OTP authorization code generating algorithm: `groupSize` (only applies to algorithm `OTP_RANDOM_DIGIT_GROUPS`)
- `timestamp_created` - timestamp when OTP policy was created

```sql
INSERT INTO ns_otp_policy (otp_policy_id, name, description, status, length, attempt_limit, expiration_time, gen_algorithm, gen_param, timestamp_created) values (1, 'OTP_POLICY', 'Sample OTP policy', 'ACTIVE', 8, 3, 300, 'OTP_DATA_DIGEST', '{}', CURRENT_TIMESTAMP);
```

The OTP policy can be created using REST API by calling the `POST /otp/policy` endpoint.

## Configuration of Next Step Credential Definitions

At least one Next Step credential definition must be configured in case credential authentication is performed. The configuration is not used when Data Adapter proxy is enabled, in this case the configuration record is prepared for potential future switch to Next Step identity once Data Adapter proxy is disabled.

The credential definition requires following configuration:
- `credential_definition_id` - credential definition identifier
- `name` - credential definition name
- `description` - description of the credential definition
- `application_id` - application identifier
- `organization_id` - organization identifier
- `credential_policy_id` - credential policy identifier
- `category` - credential category: `PASSWORD`, `PIN`, or `OTHER`
- `encryption_algorithm` - encryption protection of stored passwords use `AES_HMAC` when enabled or `NO_ENCRYPTION` 
- `hashing_enabled` - enable if the password should be hashed instead of encrypted
- `hashing_config_id` - reference to a hash configuration, required when `hashing_enabled` is true
- `data_adapter_proxy_enabled` - whether credential verification requests should be proxied via Data Adapter
- `status` - credential definition status: `ACTIVE` or `REMOVED`
- `timestamp_created` -  timestamp when credential definition was created

```sql
INSERT INTO ns_credential_definition (credential_definition_id, name, description, application_id, organization_id, credential_policy_id, category, encryption_algorithm, data_adapter_proxy_enabled, status, timestamp_created) values (1, 'RETAIL_CREDENTIAL', 'Sample credential definition for retail', 1, 'RETAIL', 1, 'PASSWORD', 'NO_ENCRYPTION', 0, 'ACTIVE', CURRENT_TIMESTAMP);
```

The Credential definition can be created using REST API by calling the `POST /credential/definition` endpoint.


## Configuration of Hashing 

For stored credentials values, i.e. passwords, the value can be hashed instead of encrypted. This allows safer storing of the value, but the actual value is not known to the server and cannot be obtained via API.

The configuration parameters:

- `hashing_config_id` - hash configuration identifier
- `name` - name of configuration
- `algorithm` - `ARGON_2D`, `ARGON_2I`, `ARGON_2ID`, `BCRYPT`
- `status` - hash configuration status: `ACTIVE` or `REMOVED`
- `parameters` - for BCRYPT use empty parameterization otherwise use argon related parameters

```sql
INSERT INTO ns_hashing_config (hashing_config_id, name, algorithm, status, parameters) values (1, 'ARGON', 'ARGON_2ID', 'ACTIVE', '{"version":"16","iterations":"3","memory":"15","parallelism":"16","outputLength":"32"}' )
```

The hashing configuration can be created using REST API by calling the `POST /hashconfig` endpoint.


## Configuration of Next Step OTP Definitions

At least one Next Step OTP definition must be configured in case OTP authentication is performed. The configuration is not used when Data Adapter proxy is enabled, in this case the configuration record is prepared for potential future switch to Next Step identity once Data Adapter proxy is disabled.

The OTP definition requires following configuration:
- `otp_definition_id` - OTP definition identifier
- `name` - OTP definition name
- `description` - description of the OTP definition
- `application_id` - application identifier
- `otp_policy_id` - OTP policy identifier
- `data_adapter_proxy_enabled` - whether OTP verification requests should be proxied via Data Adapter
- `status` - OTP definition status: `ACTIVE` or `REMOVED`
- `timestamp_created` -  timestamp when OTP definition was created

```sql
INSERT INTO ns_otp_definition (otp_definition_id, name, description, application_id, otp_policy_id, data_adapter_proxy_enabled, status, timestamp_created) values (1, 'RETAIL_OTP', 'Sample OTP definition for retail', 1, 1, 0, 'ACTIVE', CURRENT_TIMESTAMP);
```

The OTP definition can be created using REST API by calling the `POST /otp/definition` endpoint.