# Migration from 0.22.0 to 0.23.0

## Changes Since 0.22.0

### Database Changes

Following database changes were introduced in version `0.23.0`:
 
- We added `afs_enabled` column to table `ns_operation_config`
  
DDL update script for Oracle:
```
ALTER TABLE ns_operation_config ADD afs_enabled NUMBER(1) DEFAULT 0 NOT NULL;
```

DDL update script for MySQL:
```sql
ALTER TABLE `ns_operation_config` ADD `afs_enabled` BOOLEAN NOT NULL DEFAULT FALSE;
```

