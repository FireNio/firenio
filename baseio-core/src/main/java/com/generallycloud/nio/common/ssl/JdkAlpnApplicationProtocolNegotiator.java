package com.generallycloud.nio.common.ssl;

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.common.ssl.JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory;
import com.generallycloud.nio.common.ssl.JdkApplicationProtocolNegotiator.ProtocolSelectorFactory;
import com.generallycloud.nio.common.ssl.JdkApplicationProtocolNegotiator.SslEngineWrapperFactory;

/**
 * The {@link JdkApplicationProtocolNegotiator} to use if you need ALPN and are using {@link SslProvider#JDK}.
 */
public final class JdkAlpnApplicationProtocolNegotiator extends JdkBaseApplicationProtocolNegotiator {
    private static final SslEngineWrapperFactory ALPN_WRAPPER = new SslEngineWrapperFactory() {
        {
            if (!JdkAlpnSslEngine.isAvailable()) {
                throw new RuntimeException("ALPN unsupported. Is your classpatch configured correctly?"
                        + " See http://www.eclipse.org/jetty/documentation/current/alpn-chapter.html#alpn-starting");
            }
        }

        @Override
        public SSLEngine wrapSslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator,
                boolean isServer) {
            return new JdkAlpnSslEngine(engine, applicationNegotiator, isServer);
        }
    };

    /**
     * Create a new instance.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(Iterable<String> protocols) {
        this(false, protocols);
    }

    /**
     * Create a new instance.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(String... protocols) {
        this(false, protocols);
    }

    /**
     * Create a new instance.
     * @param failIfNoCommonProtocols Fail with a fatal alert if not common protocols are detected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(boolean failIfNoCommonProtocols, Iterable<String> protocols) {
        this(failIfNoCommonProtocols, failIfNoCommonProtocols, protocols);
    }

    /**
     * Create a new instance.
     * @param failIfNoCommonProtocols Fail with a fatal alert if not common protocols are detected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(boolean failIfNoCommonProtocols, String... protocols) {
        this(failIfNoCommonProtocols, failIfNoCommonProtocols, protocols);
    }

    /**
     * Create a new instance.
     * @param clientFailIfNoCommonProtocols Client side fail with a fatal alert if not common protocols are detected.
     * @param serverFailIfNoCommonProtocols Server side fail with a fatal alert if not common protocols are detected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(boolean clientFailIfNoCommonProtocols,
            boolean serverFailIfNoCommonProtocols, Iterable<String> protocols) {
        this(serverFailIfNoCommonProtocols ? FAIL_SELECTOR_FACTORY : NO_FAIL_SELECTOR_FACTORY,
                clientFailIfNoCommonProtocols ? FAIL_SELECTION_LISTENER_FACTORY : NO_FAIL_SELECTION_LISTENER_FACTORY,
                protocols);
    }

    /**
     * Create a new instance.
     * @param clientFailIfNoCommonProtocols Client side fail with a fatal alert if not common protocols are detected.
     * @param serverFailIfNoCommonProtocols Server side fail with a fatal alert if not common protocols are detected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(boolean clientFailIfNoCommonProtocols,
            boolean serverFailIfNoCommonProtocols, String... protocols) {
        this(serverFailIfNoCommonProtocols ? FAIL_SELECTOR_FACTORY : NO_FAIL_SELECTOR_FACTORY,
                clientFailIfNoCommonProtocols ? FAIL_SELECTION_LISTENER_FACTORY : NO_FAIL_SELECTION_LISTENER_FACTORY,
                protocols);
    }

    /**
     * Create a new instance.
     * @param selectorFactory The factory which provides classes responsible for selecting the protocol.
     * @param listenerFactory The factory which provides to be notified of which protocol was selected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(ProtocolSelectorFactory selectorFactory,
            ProtocolSelectionListenerFactory listenerFactory, Iterable<String> protocols) {
        super(ALPN_WRAPPER, selectorFactory, listenerFactory, protocols);
    }

    /**
     * Create a new instance.
     * @param selectorFactory The factory which provides classes responsible for selecting the protocol.
     * @param listenerFactory The factory which provides to be notified of which protocol was selected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(ProtocolSelectorFactory selectorFactory,
            ProtocolSelectionListenerFactory listenerFactory, String... protocols) {
        super(ALPN_WRAPPER, selectorFactory, listenerFactory, protocols);
    }
}
