package com.generallycloud.nio.common.ssl;

/**
 * An enumeration of SSL/TLS protocol providers.
 */
public enum SslProvider {
    /**
     * JDK's default implementation.
     */
    JDK,
    /**
     * OpenSSL-based implementation.
     */
    OPENSSL,
    /**
     * OpenSSL-based implementation which does not have finalizers and instead implements {@link ReferenceCounted}.
     */
    OPENSSL_REFCNT
}
