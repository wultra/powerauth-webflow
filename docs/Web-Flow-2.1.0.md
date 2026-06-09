# Migration from 2.0.0 to 2.1.0

This guide contains instructions for migration from PowerAuth Web Flow version `2.0.x` to version `2.1.0`.

## REST API

### Credential Reset

Similar to a credential update, the NextStep API endpoint `POST /credential/reset` resets credentials with source `LDAP` or `PROXY` and target `LOCAL`. The stored credential source is then updated to `LOCAL`.

## Database Changes

For convenience, you can use liquibase for your database migration.

The main Liquibase script is located in path [db/changelog/db.changelog-master.xml](db/changelog/db.changelog-master.xml).

For manual changes use SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_2.0.0_2.1.0.sql)
- [Oracle script](./sql/oracle/migration_2.0.0_2.1.0.sql)

### `audit_log` Table

Added a new indexed column `subject_id` holding an identifier linking the audit record to an entity it is related to (e.g. user ID for user-related audit records).

<!-- begin box warning -->
The auditing tables may be already updated in your database schema if the database schema is not separated for different PowerAuth applications. In case the column `audit_log.subject_id` and its index `audit_log_subject_id_idx` are already present, you can safely skip this migration step.
<!-- end -->


## Dependency Updates


### Docker Base Image Upgrade

The Docker base image has been upgraded from `ibm-semeru-runtimes:open-21.0.9_10-jre-noble` (OpenJDK 21) to `ibm-semeru-runtimes:open-jdk-25.0.3.0-jre-noble` (OpenJDK 25).

