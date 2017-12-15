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

public class WebSocketProtocolFactory implements ProtocolFactory {

    public static WebSocketProtocolFactory WS_PROTOCOL_FACTORY;
    public static ProtocolDecoder          WS_PROTOCOL_DECODER;
    public static ProtocolEncoder          WS_PROTOCOL_ENCODER;

    @Override
    public void initialize(SocketChannelContext context) {
        WS_PROTOCOL_FACTORY = this;
        WS_PROTOCOL_ENCODER = getProtocolEncoder(context);
        WS_PROTOCOL_DECODER = getProtocolDecoder(context);
    }

    public static final String PROTOCOL_ID = "WebSocket";

    private int                limit;

    public WebSocketProtocolFactory() {
        this(1024 * 8);
    }

    public WebSocketProtocolFactory(int limit) {
        this.limit = limit;
    }

    @Override
    public ProtocolDecoder getProtocolDecoder(SocketChannelContext context) {
        return new WebSocketProtocolDecoder(limit);
    }

    @Override
    public ProtocolEncoder getProtocolEncoder(SocketChannelContext context) {
        return new WebSocketProtocolEncoder();
    }

    @Override
    public String getProtocolId() {
        return PROTOCOL_ID;
    }
    
    public static void init(SocketChannelContext context,int limit){
        new WebSocketProtocolFactory(limit).initialize(context);
    }
    
}
