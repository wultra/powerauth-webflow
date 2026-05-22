# Next Step Server Installation

You can obtain the WAR file which can be deployed to a Java EE container in [releases](https://github.com/wultra/powerauth-webflow/releases), or use an OCI compatible image stored on [Docker Hub](https://hub.docker.com/r/powerauth/nextstep/).

Next Step Server expects an available database. The database objects should be created using the `nextstep-init` container following the standard init container pattern. The init container image is available on [Docker Hub](https://hub.docker.com/r/powerauth/nextstep-init).

## Prepare the Database

Before you deploy Next Step Server, make sure the target database is available and accessible from the container runtime.

The database objects should be created by the `nextstep-init` container using a database owner account.

## Pull the Docker Image

To deploy the Docker image, pull the image from Docker Hub:

```sh
docker pull powerauth/nextstep:latest
```

Pull the init container image as well:

```sh
docker pull powerauth/nextstep-init:latest
```

## Configure the Containers

### Configure the Initialization Container

Use the `nextstep-init` container to create database objects before the main Next Step Server container starts.

This follows the standard init container pattern:
- run the init container first
- wait until the database objects are created successfully
- start the main `powerauth/nextstep` container only after the initialization finishes

Configure the `nextstep-init` container using the database owner connection details:

```properties
NEXTSTEP_DATASOURCE_URL=jdbc:oracle:thin:@example.com:1521:sid
NEXTSTEP_DATASOURCE_USERNAME=username_owner
NEXTSTEP_DATASOURCE_PASSWORD=password_owner
```

Optionally, you can keep the init container running after the migration completes by setting:

```properties
KEEP_RUNNING=true
KEEP_RUNNING_PORT=666
```

This is useful in cloud environments where a completed init container that exits with code `0` may be restarted by the platform. When `KEEP_RUNNING=true`, the container stays alive by listening on the configured port after the migration finishes, preventing unwanted restarts. The port can also be used as a health check endpoint, a successful TCP connection or HTTP response indicates the migration has completed.

### Configure the Application Container

Configure the main Next Step Server container using environment variables:

```properties
NEXTSTEP_DATASOURCE_URL=jdbc:oracle:thin:@example.com:1521:sid
NEXTSTEP_DATASOURCE_USERNAME=username_runtime
NEXTSTEP_DATASOURCE_PASSWORD=password_runtime
```

#### API Authorization
Use the following environment variables to configure access to the Next Step Server API:

| Environment Variable                   | Default value | Description                                                                  |
|----------------------------------------|---------------|------------------------------------------------------------------------------|
| `NEXTSTEP_AUTH_TYPE`                   | `NONE`        | Set to `OIDC` to enable API authorization using OpenID Connect.              |
| `NEXTSTEP_SECURITY_AUTH_OIDC_ISSUER_URI` |               | URL of the OIDC provider, for example `https://sts.windows.net/example/`.    |
| `NEXTSTEP_SECURITY_AUTH_OIDC_AUDIENCES`  |               | A comma-separated list of allowed `aud` JWT claim values to be validated.    |

When `NEXTSTEP_AUTH_TYPE=OIDC`, configure both `NEXTSTEP_SECURITY_AUTH_OIDC_ISSUER_URI` and `NEXTSTEP_SECURITY_AUTH_OIDC_AUDIENCES`.

#### Encryption Protection

Use the following environment variables to configure additional encryption protection:

| Environment Variable                  | Description                                                                                     |
|---------------------------------------|-------------------------------------------------------------------------------------------------|
| `NEXTSTEP_MASTER_DB_ENCRYPTION_KEY`   | Base64-encoded symmetric key used for additional application-level encryption of sensitive database records. |
| `NEXTSTEP_E2E_ENCRYPTION_KEY`         | Base64-encoded symmetric key used for end-to-end encryption of password values transferred via the API.      |

Use different values for these two keys. Do not reuse the same key for database record encryption and end-to-end API encryption. Store the keys securely, ideally using a vault mechanism.

#### LDAP Adapter Configuration

Next Step Server can verify passwords against an external LDAP server. When your deployment uses LDAP-backed authentication, credential verification can be optionally proxied to LDAP.

By default, LDAP integration is disabled and all LDAP settings are ignored.

The `NEXTSTEP_LDAP_*` environment variable names indicate configuration used for the LDAP feature.

Use the following environment variables to configure LDAP:

| Environment Variable                  | Default value | Description                                 |
|---------------------------------------|---------------|---------------------------------------------|
| `NEXTSTEP_LDAP_ENABLED`               | `false`       | Enables LDAP integration.                   |
| `NEXTSTEP_LDAP_URL`                   |               | LDAP server URL.                            |
| `NEXTSTEP_LDAP_BASE`                  |               | Base DN used for LDAP operations.           |
| `NEXTSTEP_LDAP_MANAGERDN`             |               | Manager DN used for LDAP binding.           |
| `NEXTSTEP_LDAP_MANAGERPASSWORD`       |               | Manager password used for LDAP binding.     |
| `NEXTSTEP_LDAP_ANONYMOUSREADONLY`     | `false`       | Enables anonymous read-only LDAP access.    |
| `NEXTSTEP_LDAP_USERSEARCHBASE`        | `ou=People`   | Search base for LDAP user lookup.           |
| `NEXTSTEP_LDAP_USERSEARCHFILTER`      | `(uid={0})`   | LDAP filter used to find the user entry.    |
| `NEXTSTEP_LDAP_CONNECTTIMEOUT`        | `5000`        | LDAP connect timeout in milliseconds.       |
| `NEXTSTEP_LDAP_READTIMEOUT`           | `5000`        | LDAP read timeout in milliseconds.          |

Configure LDAP only if password verification should be delegated to the external LDAP server.


## Read Next

- [Configuring Next Step](./Next-Step-Server-Configuration.md)