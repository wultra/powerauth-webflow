# Migration from 0.23.0 to 0.24.0

## Database Changes

Following database changes were introduced in version `1.0.0`:
 
- Allowed nullability of column `external_id` in table `tpp_user_consent` 
  
DDL update script for Oracle:
```sql
ALTER TABLE tpp_user_consent MODIFY external_id VARCHAR(256);
```

DDL update script for MySQL:
```sql
ALTER TABLE tpp_user_consent MODIFY external_id VARCHAR(256);
```

DDL update script for PostgreSQL:
```sql
ALTER TABLE tpp_user_consent ALTER COLUMN external_id DROP NOT NULL;
```

## Upgrade to React 16

The Web application has been migrated to React version 16. 

As part of the migration, the following definition needs to be changed in `customization.css`:
- The `panel-body` class is no longer used in React 16. Replace all occurrences of `panel-body` with `panel-default`.
- It is possible that no such customization is done, in this case skip this upgrade step.

## Migration of PowerAuth client to REST interface

PowerAuth client uses REST interface in version `1.0.0`. Previous versions of Web Flow used the SOAP interface. This change needs to be reflected in configuration property `powerauth.service.url`.

Property value before migration:
`powerauth.service.url=http://[server]:[port]/powerauth-java-server/soap`

Property value after migration:
`powerauth.service.url=http://[server]:[port]/powerauth-java-server/rest`
