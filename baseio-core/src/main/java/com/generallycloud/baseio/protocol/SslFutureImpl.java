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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.ssl.SslHandler;

public class SslFutureImpl extends AbstractChannelFuture implements SslFuture {

    private boolean       body_complete;

    private boolean       header_complete;

    private int           limit;

    private SocketChannel channel;

    public SslFutureImpl(SocketChannel channel, ByteBuf buf, int limit) {
        super(channel.getContext());
        this.buf = buf;
        this.limit = limit;
        this.channel = channel;
    }

    private void doBodyComplete(ByteBuf buf) throws IOException {

        body_complete = true;

        buf.flip();

        SslHandler handler = channel.getSslHandler();

        try {

            this.buf = handler.unwrap(channel, buf);

        } finally {
            ReleaseUtil.release(buf);
        }
    }

    private void doHeaderComplete(SocketChannel channel, ByteBuf buf) throws IOException {

        header_complete = true;

        int packetLength = getEncryptedPacketLength(buf);

        buf.reallocate(packetLength, limit, true);
    }

    private int getEncryptedPacketLength(ByteBuf buffer) {
        int packetLength = 0;
        int offset = 0;
        // FIXME offset

        // SSLv3 or TLS - Check ContentType
        boolean tls;
        switch (buffer.getUnsignedByte(offset)) {
            case SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC:
            case SSL_CONTENT_TYPE_ALERT:
            case SSL_CONTENT_TYPE_HANDSHAKE:
            case SSL_CONTENT_TYPE_APPLICATION_DATA:
                tls = true;
                break;
            default:
                // SSLv2 or bad data
                tls = false;
        }

        if (tls) {
            // SSLv3 or TLS - Check ProtocolVersion
            int majorVersion = buffer.getUnsignedByte(offset + 1);
            if (majorVersion == 3) {
                // SSLv3 or TLS
                packetLength = buffer.getUnsignedShort(offset + 3) + SSL_RECORD_HEADER_LENGTH;
                if (packetLength <= SSL_RECORD_HEADER_LENGTH) {
                    // Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
                    tls = false;
                }
            } else {
                // Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
                tls = false;
            }
        }

        if (!tls) {
            // SSLv2 or bad data - Check the version
            int headerLength = (buffer.getUnsignedByte(offset) & 0x80) != 0 ? 2 : 3;
            int majorVersion = buffer.getUnsignedByte(offset + headerLength + 1);
            if (majorVersion == 2 || majorVersion == 3) {
                // SSLv2
                if (headerLength == 2) {
                    packetLength = (buffer.getShort(offset) & 0x7FFF) + 2;
                } else {
                    packetLength = (buffer.getShort(offset) & 0x3FFF) + 3;
                }
                if (packetLength <= headerLength) {
                    return -1;
                }
            } else {
                return -1;
            }
        }
        return packetLength;
    }

    @Override
    public ByteBuf getProduce() {
        return buf;
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {

        if (!header_complete) {

            ByteBuf buf = this.buf;

            buf.read(buffer);

            if (buf.hasRemaining()) {
                return false;
            }

            doHeaderComplete(channel, buf);
        }

        if (!body_complete) {

            ByteBuf buf = this.buf;

            buf.read(buffer);

            if (buf.hasRemaining()) {
                return false;
            }

            doBodyComplete(buf);
        }

        return true;
    }

}
