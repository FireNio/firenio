/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.component.ssl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLHandshakeException;

class JdkBaseApplicationProtocolNegotiator implements JdkApplicationProtocolNegotiator {

    static final ProtocolSelectionListenerFactory  FAIL_SELECTION_LISTENER_FACTORY    = new FAIL_SELECTION_LISTENER_FACTORY();
    static final ProtocolSelectorFactory           FAIL_SELECTOR_FACTORY              = new FAIL_SELECTOR_FACTORY();
    static final ProtocolSelectionListenerFactory  NO_FAIL_SELECTION_LISTENER_FACTORY = new NO_FAIL_SELECTION_LISTENER_FACTORY();
    static final ProtocolSelectorFactory           NO_FAIL_SELECTOR_FACTORY           = new NO_FAIL_SELECTOR_FACTORY();
    private final ProtocolSelectionListenerFactory listenerFactory;
    private final List<String>                     protocols;
    private final ProtocolSelectorFactory          selectorFactory;
    private final SslEngineWrapperFactory          wrapperFactory;

    protected JdkBaseApplicationProtocolNegotiator(SslEngineWrapperFactory wrapperFactory,
            ProtocolSelectorFactory selectorFactory,
            ProtocolSelectionListenerFactory listenerFactory, List<String> protocols) {
        this.wrapperFactory = wrapperFactory;
        this.selectorFactory = selectorFactory;
        this.listenerFactory = listenerFactory;
        this.protocols = Collections.unmodifiableList(protocols);
    }

    @Override
    public ProtocolSelectionListenerFactory protocolListenerFactory() {
        return listenerFactory;
    }

    @Override
    public List<String> protocols() {
        return protocols;
    }

    @Override
    public ProtocolSelectorFactory protocolSelectorFactory() {
        return selectorFactory;
    }

    @Override
    public SslEngineWrapperFactory wrapperFactory() {
        return wrapperFactory;
    }

    static class FAIL_SELECTION_LISTENER_FACTORY implements ProtocolSelectionListenerFactory {

        @Override
        public ProtocolSelectionListener newListener(SSLEngine engine,
                List<String> supportedProtocols) {
            return new FailProtocolSelectionListener((JdkSslEngine) engine, supportedProtocols);
        }

    }

    static class FAIL_SELECTOR_FACTORY implements ProtocolSelectorFactory {
        @Override
        public ProtocolSelector newSelector(SSLEngine engine, Set<String> supportedProtocols) {
            return new FailProtocolSelector((JdkSslEngine) engine, supportedProtocols);
        }

    }

    protected static final class FailProtocolSelectionListener
            extends NoFailProtocolSelectionListener {
        public FailProtocolSelectionListener(JdkSslEngine jettyWrapper,
                List<String> supportedProtocols) {
            super(jettyWrapper, supportedProtocols);
        }

        @Override
        public void noSelectedMatchFound(String protocol) throws Exception {
            throw new SSLHandshakeException("No compatible protocols found");
        }
    }

    protected static final class FailProtocolSelector extends NoFailProtocolSelector {
        public FailProtocolSelector(JdkSslEngine jettyWrapper, Set<String> supportedProtocols) {
            super(jettyWrapper, supportedProtocols);
        }

        @Override
        public String noSelectMatchFound() throws Exception {
            throw new SSLHandshakeException("Selected protocol is not supported");
        }
    }

    static class NO_FAIL_SELECTION_LISTENER_FACTORY implements ProtocolSelectionListenerFactory {

        @Override
        public ProtocolSelectionListener newListener(SSLEngine engine,
                List<String> supportedProtocols) {
            return new NoFailProtocolSelectionListener((JdkSslEngine) engine, supportedProtocols);
        }

    }

    static class NO_FAIL_SELECTOR_FACTORY implements ProtocolSelectorFactory {
        @Override
        public ProtocolSelector newSelector(SSLEngine engine, Set<String> supportedProtocols) {
            return new NoFailProtocolSelector((JdkSslEngine) engine, supportedProtocols);
        }

    }

    protected static class NoFailProtocolSelectionListener implements ProtocolSelectionListener {
        private final JdkSslEngine sslEngine;
        private final List<String> supportedProtocols;

        public NoFailProtocolSelectionListener(JdkSslEngine sslEngine,
                List<String> supportedProtocols) {
            this.sslEngine = sslEngine;
            this.supportedProtocols = supportedProtocols;
        }

        public void noSelectedMatchFound(String protocol) throws Exception {}

        @Override
        public void selected(String protocol) throws Exception {
            if (supportedProtocols.contains(protocol)) {
                sslEngine.getSession().setApplicationProtocol(protocol);
            } else {
                noSelectedMatchFound(protocol);
            }
        }

        @Override
        public void unsupported() {
            sslEngine.getSession().setApplicationProtocol(null);
        }
    }

    protected static class NoFailProtocolSelector implements ProtocolSelector {
        private final JdkSslEngine sslEngine;
        private final Set<String>  supportedProtocols;

        public NoFailProtocolSelector(JdkSslEngine sslEngine, Set<String> supportedProtocols) {
            this.sslEngine = sslEngine;
            this.supportedProtocols = supportedProtocols;
        }

        public String noSelectMatchFound() throws Exception {
            sslEngine.getSession().setApplicationProtocol(null);
            return null;
        }

        @Override
        public String select(List<String> protocols) throws Exception {
            for (String p : supportedProtocols) {
                if (protocols.contains(p)) {
                    sslEngine.getSession().setApplicationProtocol(p);
                    return p;
                }
            }
            return noSelectMatchFound();
        }

        @Override
        public void unsupported() {
            sslEngine.getSession().setApplicationProtocol(null);
        }
    }

}
