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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.protocol.ProtocolEncoder;

/**
 * @author wangkai
 *
 */
public abstract class AbstractHttpProtocolEncoder implements ProtocolEncoder {

    protected static final byte[] RN    = "\r\n".getBytes();
    protected static final byte   COLON = ':';
    protected static final byte   SPACE = ' ';

    protected void writeBuf(ByteBuf buf, byte[] array) {
        writeBuf(buf, array, 0, array.length);
    }

    protected void writeBuf(ByteBuf buf, byte[] array, int offset, int len) {
        if (buf.remaining() < len) {
            buf.reallocate(buf.position() + len, true);
            buf.limit(buf.capacity());
        }
        buf.put(array);
    }

    protected void writeBuf(ByteBuf buf, byte b) {
        if (!buf.hasRemaining()) {
            buf.reallocate(buf.capacity() + 1, true);
            buf.limit(buf.capacity());
        }
        buf.putByte(b);
    }

    protected void writeHeaders(HttpFuture f, ByteBuf buf) {

        Map<String, String> headers = f.getResponseHeaders();

        if (headers == null) {
            return;
        }

        Set<Entry<String, String>> hs = headers.entrySet();

        for (Entry<String, String> header : hs) {
            writeBuf(buf, header.getKey().getBytes());
            writeBuf(buf, COLON);
            writeBuf(buf, header.getValue().getBytes());
            writeBuf(buf, RN);
        }
    }

}
