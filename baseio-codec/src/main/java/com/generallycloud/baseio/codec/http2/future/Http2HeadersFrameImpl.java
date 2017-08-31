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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.http2.Http2SocketSession;
import com.generallycloud.baseio.codec.http2.hpack.Decoder;
import com.generallycloud.baseio.component.SocketChannel;

public class Http2HeadersFrameImpl extends AbstractHttp2Frame implements Http2HeadersFrame {

    private boolean        isComplete;

    private byte           padLength;

    private boolean        e;

    private int            streamDependency;

    private short          weight;

    private boolean        endStream;

    private static Decoder decoder = new Decoder();

    public Http2HeadersFrameImpl(SocketChannel channel, ByteBuf buf, Http2FrameHeader header) {
        super(channel, header);
        this.buf = buf;
    }

    private void doComplete(SocketChannel channel, ByteBuf buf) throws IOException {

        Http2SocketSession session = (Http2SocketSession) channel.getSession();
        
        byte flags = getHeader().getFlags();

        this.endStream = (flags & FLAG_END_STREAM) > 0;

        if ((flags & FLAG_PADDED) > 0) {
            padLength = buf.getByte();
        }

        if ((flags & FLAG_PRIORITY) > 0) {

            streamDependency = buf.getInt();

            e = streamDependency < 0;

            if (e) {
                streamDependency = streamDependency & 0x7FFFFFFF;
            }

            weight = buf.getUnsignedByte();
        }

        decoder.decode(streamDependency, buf, session.getHttp2Headers());
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {

        if (!isComplete) {

            ByteBuf buf = this.buf;

            buf.read(buffer);

            if (buf.hasRemaining()) {
                return false;
            }

            this.isComplete = true;

            doComplete(channel, buf.flip());
        }

        return true;
    }

    @Override
    public boolean isSilent() {
        return !endStream;
    }

    @Override
    public Http2FrameType getHttp2FrameType() {
        return Http2FrameType.FRAME_TYPE_HEADERS;
    }

    @Override
    public boolean isE() {
        return e;
    }

    @Override
    public int getStreamDependency() {
        return streamDependency;
    }

    @Override
    public short getWeight() {
        return weight;
    }

    @Override
    public byte getPadLength() {
        return padLength;
    }

}
