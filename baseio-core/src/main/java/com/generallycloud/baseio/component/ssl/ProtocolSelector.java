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

import java.util.List;

/**
 * @author wangkai
 *
 */
public interface ProtocolSelector {

    /**
      * Callback invoked to let the application know that the peer does not support this
      * {@link ApplicationProtocolNegotiator}.
      */
    void unsupported();

    /**
     * Callback invoked to select the application level protocol from the {@code protocols} provided.
     *
     * @param protocols the protocols sent by the protocol advertiser
     * @return the protocol selected by this {@link ProtocolSelector}. A {@code null} value will indicate the no
     * protocols were selected but the handshake should not fail. The decision to fail the handshake is left to the
     * other end negotiating the SSL handshake.
     * @throws Exception If the {@code protocols} provide warrant failing the SSL handshake with a fatal alert.
     */
    String select(List<String> protocols) throws Exception;

}
