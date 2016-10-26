package com.generallycloud.nio.common.ssl;

import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLEngine;

import static com.generallycloud.nio.common.ssl.ApplicationProtocolUtil.toList;

/**
 * Provides an {@link SSLEngine} agnostic way to configure a {@link ApplicationProtocolNegotiator}.
 */
public final class ApplicationProtocolConfig {

    /**
     * The configuration that disables application protocol negotiation.
     */
    public static final ApplicationProtocolConfig DISABLED = new ApplicationProtocolConfig();

    private final List<String> supportedProtocols;
    private final Protocol protocol;
    private final SelectorFailureBehavior selectorBehavior;
    private final SelectedListenerFailureBehavior selectedBehavior;

    /**
     * Create a new instance.
     * @param protocol The application protocol functionality to use.
     * @param selectorBehavior How the peer selecting the protocol should behave.
     * @param selectedBehavior How the peer being notified of the selected protocol should behave.
     * @param supportedProtocols The order of iteration determines the preference of support for protocols.
     */
    public ApplicationProtocolConfig(Protocol protocol, SelectorFailureBehavior selectorBehavior,
            SelectedListenerFailureBehavior selectedBehavior, Iterable<String> supportedProtocols) {
        this(protocol, selectorBehavior, selectedBehavior, toList(supportedProtocols));
    }

    /**
     * Create a new instance.
     * @param protocol The application protocol functionality to use.
     * @param selectorBehavior How the peer selecting the protocol should behave.
     * @param selectedBehavior How the peer being notified of the selected protocol should behave.
     * @param supportedProtocols The order of iteration determines the preference of support for protocols.
     */
    public ApplicationProtocolConfig(Protocol protocol, SelectorFailureBehavior selectorBehavior,
            SelectedListenerFailureBehavior selectedBehavior, String... supportedProtocols) {
        this(protocol, selectorBehavior, selectedBehavior, toList(supportedProtocols));
    }

    /**
     * Create a new instance.
     * @param protocol The application protocol functionality to use.
     * @param selectorBehavior How the peer selecting the protocol should behave.
     * @param selectedBehavior How the peer being notified of the selected protocol should behave.
     * @param supportedProtocols The order of iteration determines the preference of support for protocols.
     */
    private ApplicationProtocolConfig(
            Protocol protocol, SelectorFailureBehavior selectorBehavior,
            SelectedListenerFailureBehavior selectedBehavior, List<String> supportedProtocols) {
        this.supportedProtocols = Collections.unmodifiableList(supportedProtocols);
        this.protocol = protocol;
        this.selectorBehavior = selectorBehavior;
        this.selectedBehavior = selectedBehavior;

        if (protocol == Protocol.NONE) {
            throw new IllegalArgumentException("protocol (" + Protocol.NONE + ") must not be " + Protocol.NONE + '.');
        }
        if (supportedProtocols.isEmpty()) {
            throw new IllegalArgumentException("supportedProtocols must be not empty");
        }
    }

    /**
     * A special constructor that is used to instantiate {@link #DISABLED}.
     */
    private ApplicationProtocolConfig() {
        supportedProtocols = Collections.emptyList();
        protocol = Protocol.NONE;
        selectorBehavior = SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL;
        selectedBehavior = SelectedListenerFailureBehavior.ACCEPT;
    }

    /**
     * Defines which application level protocol negotiation to use.
     */
    public enum Protocol {
        NONE, NPN, ALPN, NPN_AND_ALPN
    }

    /**
     * Defines the most common behaviors for the peer that selects the application protocol.
     */
    public enum SelectorFailureBehavior {
        FATAL_ALERT, NO_ADVERTISE, CHOOSE_MY_LAST_PROTOCOL
    }

    /**
     * Defines the most common behaviors for the peer which is notified of the selected protocol.
     */
    public enum SelectedListenerFailureBehavior {
        ACCEPT, FATAL_ALERT, CHOOSE_MY_LAST_PROTOCOL
    }

    /**
     * The application level protocols supported.
     */
    public List<String> supportedProtocols() {
        return supportedProtocols;
    }

    /**
     * Get which application level protocol negotiation to use.
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * Get the desired behavior for the peer who selects the application protocol.
     */
    public SelectorFailureBehavior selectorFailureBehavior() {
        return selectorBehavior;
    }

    /**
     * Get the desired behavior for the peer who is notified of the selected protocol.
     */
    public SelectedListenerFailureBehavior selectedListenerFailureBehavior() {
        return selectedBehavior;
    }
}
