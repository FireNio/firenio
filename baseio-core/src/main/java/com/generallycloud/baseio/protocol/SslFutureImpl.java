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
package com.generallycloud.baseio.protocol;

import java.io.IOException;

import javax.net.ssl.SSLException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.component.SocketChannel;

public class SslFutureImpl extends AbstractChannelFuture implements SslFuture {

    private boolean header_complete;
    private int     limit;

    public SslFutureImpl(ByteBuf buf, int limit) {
        this.buf = buf;
        this.limit = limit;
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {
        if (!header_complete) {
            ByteBuf buf = this.buf;
            buf.read(buffer);
            if (buf.hasRemaining()) {
                return false;
            }
            header_complete = true;
            // SSLv3 or TLS - Check ContentType
            short type = buf.getUnsignedByte(0);
            if (type < 20 || type > 23) {
                throw new SSLException("Neither SSLv3 or TLS");
            }
            // SSLv3 or TLS - Check ProtocolVersion
            int majorVersion = buf.getUnsignedByte(1);
            int packetLength = buf.getUnsignedShort(3);
            if (majorVersion != 3 || packetLength <= 0) {
                throw new SSLException("Neither SSLv3 or TLS");
            } else {
                // Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
            }
            buf.reallocate(packetLength + 5, limit, true);
        }
        ByteBuf buf = this.buf;
        buf.read(buffer);
        if (buf.hasRemaining()) {
            return false;
        }
        buf.flip();
        return true;
    }

    @Override
    public SslFuture reset() {
        this.header_complete = false;
        buf.clear().limit(SSL_RECORD_HEADER_LENGTH);
        return this;
    }

    @Override
    public SslFuture copy(SocketChannel channel) {
        ByteBuf src = getByteBuf();
        ByteBuf buf = allocate(channel, src.limit());
        buf.read(src.flip());
        SslFutureImpl copy = new SslFutureImpl(buf, 1024 * 64);
        copy.header_complete = this.header_complete;
        return copy;
    }

}
