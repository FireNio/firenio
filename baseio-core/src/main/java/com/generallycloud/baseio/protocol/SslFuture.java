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
import com.generallycloud.baseio.component.NioSocketChannel;

/**
 * <pre>
 *  B0: type >> ALERT APPLICATION_DATA CHANGE_CIPHER_SPEC HANDSHAKE UnsignedByte
 *  B1: Major Version UnsignedByte
 *  B2: Minor Version UnsignedByte
 *  B3-B4: Length UnsignedShort
 * </pre>
 */
public class SslFuture extends AbstractFuture {

    public static int SSL_CONTENT_TYPE_ALERT              = 21;

    public static int SSL_CONTENT_TYPE_APPLICATION_DATA   = 23;

    public static int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20;

    public static int SSL_CONTENT_TYPE_HANDSHAKE          = 22;

    public static int SSL_RECORD_HEADER_LENGTH            = 5;

    private ByteBuf   buf;

    public SslFuture(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public boolean read(NioSocketChannel channel, ByteBuf src) throws IOException {
        if (src.remaining() < 5) {
            return false;
        }
        // SSLv3 or TLS - Check ContentType
        int type = src.getUnsignedByte();
        if (type < 20 || type > 23) {
            throw new SSLException("Neither SSLv3 or TLS");
        }
        // SSLv3 or TLS - Check ProtocolVersion
        int majorVersion = src.getUnsignedByte();
        int minorVersion = src.getUnsignedByte();
        int packetLength = src.getUnsignedShort();
        if (majorVersion != 3 || packetLength <= 0) {
            throw new SSLException("Neither SSLv3 or TLS");
        } else {
            // Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
        }
        int len = packetLength;
        if (src.remaining() < len) {
            src.skip(-5);
            return false;
        }
        if (len > 1024 * 64 - 5) {
            throw new IOException("over limit:" + len);
        }
        src.skip(-5);
        buf.clear();
        buf.limit(len + 5);
        buf.read(src);
        buf.flip();
        return true;
    }

    @Override
    public SslFuture reset() {
        return this;
    }

    public ByteBuf getByteBuf() {
        return buf;
    }

}
