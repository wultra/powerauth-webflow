package io.getlime.security.powerauth.lib.dataadapter.model.enumeration;

/**
 * Certificate authentication modes:
 * - ENABLED = certificate can be used and UI shows the option
 * - DISABLED = certificate could be normally used, but the UI option is disabled for this case
 * - NOT_AVAILABLE = certificate authentication is not available
 */
public enum CertificateAuthenticationMode {
    ENABLED,
    DISABLED,
    NOT_AVAILABLE
}
