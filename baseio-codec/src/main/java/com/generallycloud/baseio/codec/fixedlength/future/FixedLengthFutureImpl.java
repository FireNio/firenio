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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthProtocolDecoder;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolException;

public class FixedLengthFutureImpl extends AbstractChannelFuture implements FixedLengthFuture {

    private boolean header_complete;

    private boolean body_complete;

    private int     limit;

    public FixedLengthFutureImpl(SocketChannel channel, ByteBuf buf, int limit) {
        super(channel.getContext());
        this.buf = buf;
        this.limit = limit;
    }

    public FixedLengthFutureImpl(SocketChannelContext context) {
        super(context);
    }

    private void doHeaderComplete(SocketChannel channel, ByteBuf buf) {

        int length = buf.getInt();

        if (length < 1) {
            body_complete = true;
            if (length == FixedLengthProtocolDecoder.PROTOCOL_PING) {
                setPING();
            } else if (length == FixedLengthProtocolDecoder.PROTOCOL_PONG) {
                setPONG();
            } else {
                throw new ProtocolException("illegal length:" + length);
            }
            return;
        }

        buf.reallocate(length, limit);
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf src) throws IOException {

        ByteBuf buf = this.buf;

        if (!header_complete) {

            buf.read(src);

            if (buf.hasRemaining()) {
                return false;
            }

            header_complete = true;

            doHeaderComplete(channel, buf.flip());
        }

        if (!body_complete) {

            buf.read(src);

            if (buf.hasRemaining()) {
                return false;
            }

            body_complete = true;

            doBodyComplete(buf.flip());
        }

        return true;
    }

    private void doBodyComplete(ByteBuf buf) throws CharacterCodingException {

        CharsetDecoder decoder = context.getEncoding().newDecoder();

        this.readText = decoder.decode(buf.nioBuffer()).toString();
    }

}
