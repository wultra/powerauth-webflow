# Migration from 1.2.0 to 1.3.0

## Database Dialect Configuration

The latest release of PowerAuth requires configuration of database dialect.

The dialect is specified using following configuration property:
```properties
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL95Dialect
```

Use the most specific dialect, if possible, such as:
- `org.hibernate.dialect.Oracle12cDialect` for Oracle 12c or higher
- `org.hibernate.dialect.PostgreSQL95Dialect` for PostgreSQL 9.5 or higher

You can find additional database dialects in Hibernate documentation.
