# Migration from 1.7.0 to 1.8.0

This guide contains instructions for migration from PowerAuth WebFlow version `1.7.x` to version `1.8.0`.

## API

### Attribute `credentialName` modification

The attribute `credentialName` has been removed from the request object of the API endpoint `/auth/combined`. This change
was made because the attribute was not utilized in the underlying functionality.
