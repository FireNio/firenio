package com.generallycloud.nio.common.ssl;

/**
 * Provides a way to get the application-level protocol name from ALPN or NPN.
 */
interface ApplicationProtocolAccessor {
    /**
     * Returns the name of the negotiated application-level protocol.
     *
     * @return the application-level protocol name or
     *         {@code null} if the negotiation failed or the client does not have ALPN/NPN extension
     */
    String getApplicationProtocol();
}
