# Migration from 1.1.0 to 1.2.0

## Database Changes

Following database changes were introduced in version `1.2.0`:

DDL update script for Oracle:

```sql
ALTER TABLE ns_operation_history ADD pa_auth_context VARCHAR2(256 CHAR);
```

DDL update script for MySQL:

```sql
ALTER TABLE ns_operation_history ADD pa_auth_context VARCHAR(256);
```

DDL update script for PostgreSQL:

```sql
ALTER TABLE ns_operation_history ADD pa_auth_context VARCHAR(256);
```

## Data Adapter Changes

The Data Adapter interface was updated to provide information about PowerAuth authentication context in method `operationChangedNotification`, following parameters are available under `operationContext.authenticationContext`:
  - `signatureType` - signature type used when verifying PowerAuth signature
    - `possession` - 1FA signature using possession key factor
    - `possession_knowledge` - 2FA signature using possession and knowledge key factors
    - `possession_biometry` - 2FA signature using possession and biometry key factors
  - `remainingAttempts` - number of remaining attempts for signature verification
  - `blocked` - whether activation was blocked during authentication attempt

Note that in case no PowerAuth authentication is executed during the operation (e.g. operation gets canceled immediately after creation), the `authenticationContext` contains `null` values for all its parameters.
