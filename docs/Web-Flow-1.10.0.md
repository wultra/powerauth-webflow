# Migration from 1.9.0 to 1.10.x

This guide contains instructions for migration from PowerAuth WebFlow version `1.9.x` to version `1.10.x`.


# 1.10.0

## Database Changes

Added table `shedlock` to prevent execution of the same task from another node.

# 1.10.1

## REST API

### Adding Credential Source and Credentual Target

Credentials object holders extends of attributes `credentialSource` and `credentialTarget` with type `credentialLocation`. 

The possible values:

- `LOCAL`: Credentials are internal in NextStep. This is a default value if not specified.
- `PROXY`: Credentials are proxied to external system.

The attribute description:

- `credentialSource`: The current location of the credentials, i.e. LOCAL store or verified in external system via Data Adapter.
- `credentialTarget`: The target location of the credentials after update, i.e. credentials can be after update migrated from PROXY to LOCAL or vice versa. Note the reset API doesn't change the location.

The changed API:

- Create a user identity
- Get user identity detail.
- Update a user identity.
- Update multiple user identities.
- Lookup a user identity.
- Lookup user identities
- Get user credential list.
- Create a credential.
- Update a credential.
