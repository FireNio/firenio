package com.generallycloud.nio.common.ssl;


/**
 * Indicates the state of the {@link javax.net.ssl.SSLEngine} with respect to client authentication.
 * This configuration item really only applies when building the server-side {@link SslContext}.
 */
public enum ClientAuth {
    /**
     * Indicates that the {@link javax.net.ssl.SSLEngine} will not request client authentication.
     */
    NONE,

    /**
     * Indicates that the {@link javax.net.ssl.SSLEngine} will request client authentication.
     */
    OPTIONAL,

    /**
     * Indicates that the {@link javax.net.ssl.SSLEngine} will *require* client authentication.
     */
    REQUIRE
}
