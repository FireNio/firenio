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
package com.generallycloud.baseio.codec.http11;

import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;

public class ClientHTTPProtocolFactory implements ProtocolFactory {
    
    @Override
    public void initialize(SocketChannelContext context) {
    }
    
    private int headerLimit;

    private int bodyLimit;

    public ClientHTTPProtocolFactory() {
        this(1024 * 8, 1024 * 512);
    }

    public ClientHTTPProtocolFactory(int headerLimit, int bodyLimit) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
    }

    @Override
    public ProtocolDecoder getProtocolDecoder(SocketChannelContext context) {
        return new ClientHTTPProtocolDecoder(headerLimit, bodyLimit);
    }

    @Override
    public ProtocolEncoder getProtocolEncoder(SocketChannelContext context) {
        return new ClientHTTPProtocolEncoder();
    }

    @Override
    public String getProtocolId() {
        return "HTTP11";
    }
}
