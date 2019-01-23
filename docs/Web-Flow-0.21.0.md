# Migration from 0.20.0 to 0.21.0

## Changes Since 0.20.0

### Migration to PowerAuth cryptography protocol version 3.0

Web Flow 0.21.0 requires PowerAuth server version 0.21.0 because of introduction of new version of cryptography protocol.

For details about cryptography changes see:
- [PowerAuth server migration guide](https://github.com/wultra/powerauth-server/blob/develop/docs/PowerAuth-Server-0.21.0.md#powerauth-protocol-version-30)
- [PowerAuth crypto project](https://github.com/wultra/powerauth-crypto)

The upgrade of Web Flow to version 0.21.0 needs to be performed after PowerAuth server upgrade, so that new web service interfaces for crypto 3.0 are available.
