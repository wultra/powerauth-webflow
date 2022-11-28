# Migration from 1.3.0 to 1.4.0

## Database Changes

Following database changes were introduced in version `1.4.0`:

DDL update script for Oracle:

```sql
ALTER TABLE tpp_detail ADD tpp_blocked NUMBER(1) DEFAULT 0 NOT NULL;
```

DDL update script for MySQL:

```sql
ALTER TABLE tpp_detail ADD tpp_blocked BOOLEAN NOT NULL DEFAULT FALSE;
```

DDL update script for PostgreSQL:

```sql
ALTER TABLE tpp_detail ADD tpp_blocked BOOLEAN DEFAULT FALSE NOT NULL;
```
