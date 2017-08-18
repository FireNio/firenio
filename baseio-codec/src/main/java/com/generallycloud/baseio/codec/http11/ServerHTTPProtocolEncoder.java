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

import java.io.IOException;
import java.util.List;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.codec.http11.future.Cookie;
import com.generallycloud.baseio.codec.http11.future.ServerHttpFuture;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.protocol.ChannelFuture;

public class ServerHTTPProtocolEncoder extends AbstractHttpProtocolEncoder {

    private static final byte[] PROTOCOL   = "HTTP/1.1 ".getBytes();
    private static final byte[] SERVER_CL  = "\r\nServer:baseio/0.0.1\r\nContent-Length:"
            .getBytes();
    private static final byte[] SET_COOKIE = "Set-Cookie:".getBytes();

    @Override
    public void encode(ByteBufAllocator allocator, ChannelFuture readFuture) throws IOException {

        ServerHttpFuture f = (ServerHttpFuture) readFuture;

        f.setResponseHeader("Date",
                HttpHeaderDateFormat.getFormat().format(System.currentTimeMillis()));

        ByteArrayBuffer os = f.getBinaryBuffer();

        if (os != null) {
            encode(allocator, f, os.size(), os.array());
            return;
        }

        ByteArrayBuffer buffer = f.getWriteBuffer();

        if (buffer == null) {
            encode(allocator, f, 0, null);
            return;
        }

        encode(allocator, f, buffer.size(), buffer.array());
    }

    private void encode(ByteBufAllocator allocator, ServerHttpFuture f, int length, byte[] array)
            throws IOException {

        ByteBuf buf = allocator.allocate(256);

        try {

            buf.put(PROTOCOL);
            buf.put(f.getStatus().getHeaderBinary());
            buf.put(SERVER_CL);
            buf.put(String.valueOf(length).getBytes());
            buf.put(RN);

            writeHeaders(f, buf);

            List<Cookie> cookieList = f.getCookieList();

            if (cookieList != null) {
                for (Cookie c : cookieList) {
                    writeBuf(buf, SET_COOKIE);
                    writeBuf(buf, c.toString().getBytes());
                    writeBuf(buf, RN);
                }
            }

            writeBuf(buf, RN);

            if (length != 0) {
                writeBuf(buf, array, 0, length);
            }

        } catch (Exception e) {
            ReleaseUtil.release(buf);
            throw e;
        }

        f.setByteBuf(buf.flip());
    }

}
