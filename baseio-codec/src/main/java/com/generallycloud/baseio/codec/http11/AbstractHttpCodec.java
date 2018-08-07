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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.protocol.ProtocolCodec;

/**
 * @author wangkai
 *
 */
public abstract class AbstractHttpCodec extends ProtocolCodec {

    protected static final byte R     = '\r';
    protected static final byte N     = '\n';
    protected static final byte COLON = ':';
    protected static final byte SPACE = ' ';

    protected void writeBuf(ByteBuf buf, byte[] array) {
        writeBuf(buf, array, 0, array.length);
    }

    protected void writeBuf(ByteBuf buf, byte[] array, int off, int len) {
        if (buf.remaining() < len) {
            buf.reallocate(buf.position() + len, true);
            buf.limit(buf.capacity());
        }
        buf.put(array, off, len);
    }

    protected void writeBuf(ByteBuf buf, byte b) {
        if (!buf.hasRemaining()) {
            buf.reallocate(buf.capacity() + 1, true);
            buf.limit(buf.capacity());
        }
        buf.putByte(b);
    }

}
