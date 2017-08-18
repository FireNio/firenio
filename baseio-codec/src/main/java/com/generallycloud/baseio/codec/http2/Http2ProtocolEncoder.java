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
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.codec.http2.future.Http2Frame;
import com.generallycloud.baseio.codec.http2.future.Http2FrameType;
import com.generallycloud.baseio.codec.http2.future.Http2HeadersFrame;
import com.generallycloud.baseio.codec.http2.future.Http2SettingsFrame;
import com.generallycloud.baseio.codec.http2.hpack.DefaultHttp2HeadersEncoder;
import com.generallycloud.baseio.codec.http2.hpack.Http2HeadersEncoder;
import com.generallycloud.baseio.common.MathUtil;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolEncoder;

public class Http2ProtocolEncoder implements ProtocolEncoder {

    private Http2HeadersEncoder http2HeadersEncoder = new DefaultHttp2HeadersEncoder();

    @Override
    public void encode(ByteBufAllocator allocator, ChannelFuture future) throws IOException {

        Http2Frame frame = (Http2Frame) future;

        Http2FrameType frameType = frame.getHttp2FrameType();

        byte[] payload = null;

        switch (frameType) {
            case FRAME_TYPE_CONTINUATION:

                break;
            case FRAME_TYPE_DATA:

                break;
            case FRAME_TYPE_FRAME_HEADER:

                break;
            case FRAME_TYPE_GOAWAY:

                break;
            case FRAME_TYPE_HEADERS:

                Http2HeadersFrame hf = (Http2HeadersFrame) frame;

                //			http2HeadersEncoder.encodeHeaders(headers, buffer);

                break;
            case FRAME_TYPE_PING:

                break;
            case FRAME_TYPE_PREFACE:
                break;
            case FRAME_TYPE_PRIORITY:

                break;
            case FRAME_TYPE_PUSH_PROMISE:

                break;
            case FRAME_TYPE_RST_STREAM:

                break;
            case FRAME_TYPE_SETTINGS:

                Http2SettingsFrame sf = (Http2SettingsFrame) frame;

                long[] settings = sf.getSettings();

                payload = new byte[6 * 6];

                for (int i = 0; i < 6; i++) {
                    int realI = i + 1;
                    int offset = i * 6;
                    MathUtil.unsignedShort2Byte(payload, realI, offset);
                    MathUtil.unsignedInt2Byte(payload, settings[realI], offset + 2);
                }

                break;
            case FRAME_TYPE_WINDOW_UPDATE:

                break;
            default:
                break;
        }

        int length = payload.length;

        ByteBuf buf = allocator.allocate(length + Http2ProtocolDecoder.PROTOCOL_HEADER);

        byte b2 = (byte) ((length & 0xff));
        byte b1 = (byte) ((length >> 8 * 1) & 0xff);
        byte b0 = (byte) ((length >> 8 * 2) & 0xff);
        byte b3 = frameType.getByteValue();

        buf.putByte(b0);
        buf.putByte(b1);
        buf.putByte(b2);
        buf.putByte(b3);
        buf.putByte((byte) 0);

        buf.putInt(frame.getHeader().getStreamIdentifier());

        buf.put(payload);

        future.setByteBuf(buf.flip());
    }

}
