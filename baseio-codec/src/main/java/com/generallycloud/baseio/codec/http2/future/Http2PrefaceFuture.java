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
package com.generallycloud.baseio.codec.http2.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.codec.http2.Http2SocketSession;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;
import com.generallycloud.baseio.protocol.DefaultChannelFuture;

public class Http2PrefaceFuture extends AbstractChannelFuture {

    private boolean        isComplete;

    private static byte[]  PREFACE_BINARY = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes();

    private static ByteBuf PREFACE_BUF;

    static {

        PREFACE_BUF = UnpooledByteBufAllocator.getHeapInstance()
                .wrap(ByteBuffer.wrap(PREFACE_BINARY));
    }

    public Http2PrefaceFuture(SocketChannelContext context, ByteBuf buf) {
        super(context);
        this.buf = buf;
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    private void doComplete(SocketChannel channel, ByteBuf buf) throws IOException {

        Http2SocketSession session = (Http2SocketSession) channel.getSession();
        
        session.setPrefaceRead(false);

        if (!isPreface(buf)) {
            throw new IOException("not http2 preface");
        }

        session.doFlush(new DefaultChannelFuture(context, PREFACE_BUF.duplicate()));
    }

    private boolean isPreface(ByteBuf buf) {

        if (PREFACE_BINARY.length > buf.remaining()) {
            return false;
        }

        for (int i = 0; i < PREFACE_BINARY.length; i++) {

            if (PREFACE_BINARY[i] != buf.getByte()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {

        ByteBuf buf = this.buf;

        if (!isComplete) {

            buf.read(buffer);

            if (buf.hasRemaining()) {
                return false;
            }

            this.isComplete = true;

            doComplete(channel, buf.flip());
        }

        return true;
    }

}
