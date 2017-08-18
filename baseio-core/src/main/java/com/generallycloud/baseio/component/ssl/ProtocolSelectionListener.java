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

/**
 * @author wangkai
 *
 */
public interface ProtocolSelectionListener {

    /**
      * Callback invoked to let the application know that the peer does not support this
      * {@link ApplicationProtocolNegotiator}.
      */
    void unsupported();

    /**
     * Callback invoked to let this application know the protocol chosen by the peer.
     *
     * @param protocol the protocol selected by the peer. May be {@code null} or empty as supported by the
     * application negotiation protocol.
     * @throws Exception This may be thrown if the selected protocol is not acceptable and the desired behavior is
     * to fail the handshake with a fatal alert.
     */
    void selected(String protocol) throws Exception;

}
