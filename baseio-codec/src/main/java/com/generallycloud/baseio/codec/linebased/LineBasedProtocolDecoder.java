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
package com.generallycloud.baseio.codec.linebased;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.linebased.future.LineBasedFutureImpl;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;

/**
 * 基于换行符\n的消息分割
 */
public class LineBasedProtocolDecoder implements ProtocolDecoder {

    public static final byte LINE_BASE = '\n';

    private int              limit;

    public LineBasedProtocolDecoder(int limit) {
        this.limit = limit;
    }

    @Override
    public ChannelFuture decode(SocketSession session, ByteBuf buffer) throws IOException {

        return new LineBasedFutureImpl(session.getContext(), limit);
    }

}
