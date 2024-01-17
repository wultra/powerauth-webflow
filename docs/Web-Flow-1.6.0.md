# Migration from 1.5.0 to 1.6.0

This guide contains instructions for migration from PowerAuth WebFlow version `1.5.x` to version `1.6.0`.

## Database Changes

For convenience you can use liquibase for your database migration.

For manual changes use SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_1.5.1_1.6.0.sql)
- [Oracle script](./sql/oracle/migration_1.5.1_1.6.0.sql)

### Fix DB Bug for Creating OTP without User ID

The column `user_id` in table `ns_otp_storage` is nullable now.

