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
package com.generallycloud.baseio.codec.fixedlength.future;

import java.io.IOException;
import java.nio.charset.CharsetDecoder;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthProtocolDecoder;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolException;

public class FixedLengthFutureImpl extends AbstractChannelFuture implements FixedLengthFuture {

    private boolean header_complete;
    private int     limit;
    private String  readText;

    public FixedLengthFutureImpl() {}

    public FixedLengthFutureImpl(int limit) {
        this.limit = limit;
    }

    @Override
    public String getReadText() {
        return readText;
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf src) throws IOException {
        ByteBuf buf = this.buf;
        if (buf == EmptyByteBuf.getInstance()) {
            if (src.remaining() >= 4) {
                int len = src.getInt();
                if (len < 1) {
                    setHeartBeat(len);
                    return true;
                }
                if (src.remaining() >= len) {
                    src.markPL();
                    src.limit(len + src.position());
                    try {
                        CharsetDecoder decoder = channel.getEncoding().newDecoder();
                        this.readText = decoder.decode(src.nioBuffer()).toString();
                    } finally {
                        src.reset();
                        src.skipBytes(len);
                    }
                    return true;
                } else {
                    header_complete = true;
                    buf = allocate(channel, len);
                    buf.read(src);
                    this.buf = buf;
                }
            } else {
                buf = allocate(channel, 4);
                buf.read(src);
                this.buf = buf;
            }
            return false;
        } else {
            if (!header_complete) {
                buf.read(src);
                if (buf.hasRemaining()) {
                    return false;
                }
                header_complete = true;
                buf.flip();
                int len = buf.getInt();
                if (len < 1) {
                    setHeartBeat(len);
                    return true;
                }
                buf.reallocate(len, limit);
            }
            buf.read(src);
            if (buf.hasRemaining()) {
                return false;
            }
            buf.flip();
            CharsetDecoder decoder = channel.getEncoding().newDecoder();
            this.readText = decoder.decode(buf.nioBuffer()).toString();
            return true;
        }
    }

    private void setHeartBeat(int len) {
        if (len == FixedLengthProtocolDecoder.PROTOCOL_PING) {
            setPING();
        } else if (len == FixedLengthProtocolDecoder.PROTOCOL_PONG) {
            setPONG();
        } else {
            throw new ProtocolException("illegal length:" + len);
        }
    }

    @Override
    public String toString() {
        return getReadText();
    }

}
