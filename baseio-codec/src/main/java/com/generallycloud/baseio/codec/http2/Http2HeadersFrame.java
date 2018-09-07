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
package com.generallycloud.baseio.codec.http2;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.http2.hpack.Decoder;

public class Http2HeadersFrame extends Http2FrameHeader {

    private byte           padLength;
    private boolean        e;
    private int            streamDependency;
    private short          weight;
    private boolean        endStream;
    private static Decoder decoder = new Decoder();

    Http2HeadersFrame decode(Http2Session session, ByteBuf src, int length) throws IOException {
        byte flags = getFlags();
        this.endStream = (flags & FLAG_END_STREAM) > 0;
        if ((flags & FLAG_PADDED) > 0) {
            padLength = src.getByte();
        }
        if ((flags & FLAG_PRIORITY) > 0) {
            streamDependency = src.getInt();
            e = streamDependency < 0;
            if (e) {
                streamDependency = streamDependency & 0x7FFFFFFF;
            }
            weight = src.getUnsignedByte();
        }
        decoder.decode(streamDependency, src, session.getHttp2Headers());
        return this;
    }

    @Override
    public boolean isSilent() {
        return !endStream;
    }

    @Override
    public Http2FrameType getHttp2FrameType() {
        return Http2FrameType.FRAME_TYPE_HEADERS;
    }

    public boolean isE() {
        return e;
    }

    public int getStreamDependency() {
        return streamDependency;
    }

    public short getWeight() {
        return weight;
    }

    public byte getPadLength() {
        return padLength;
    }

}
