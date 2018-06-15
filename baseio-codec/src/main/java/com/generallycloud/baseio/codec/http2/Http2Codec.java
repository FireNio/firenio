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
import com.generallycloud.baseio.codec.http2.future.Http2FrameHeaderImpl;
import com.generallycloud.baseio.codec.http2.future.Http2FrameType;
import com.generallycloud.baseio.codec.http2.future.Http2HeadersFrame;
import com.generallycloud.baseio.codec.http2.future.Http2PrefaceFuture;
import com.generallycloud.baseio.codec.http2.future.Http2SettingsFrame;
import com.generallycloud.baseio.codec.http2.hpack.DefaultHttp2HeadersEncoder;
import com.generallycloud.baseio.codec.http2.hpack.Http2HeadersEncoder;
import com.generallycloud.baseio.common.MathUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolCodec;

/**
 * <pre>
 * +-----------------------------------------------+
 * |                 Length (24)                   |
 * +---------------+---------------+---------------+
 * |   Type (8)    |   Flags (8)   |
 * +-+-------------+---------------+-------------------------------+
 * |R|                 Stream Identifier (31)                      |
 * +=+=============================================================+
 * |                   Frame Payload (0...)                      ...
 * +---------------------------------------------------------------+
 * </pre>
 * <dl>
 * <dt>Length:</dt>
 * <dd>
 * <p>
 * The length of the frame payload expressed as an unsigned 24-bit integer.
 * Values greater than 2<sup>14</sup> (16,384) MUST NOT be sent unless the
 * receiver has set a larger value for <a href="#SETTINGS_MAX_FRAME_SIZE"
 * class="smpl">SETTINGS_MAX_FRAME_SIZE</a>.
 * </p>
 * <p>
 * The 9 octets of the frame header are not included in this value.
 * </p>
 * </dd>
 * <dt>Type:</dt>
 * <dd>
 * <p>
 * The 8-bit type of the frame. The frame type determines the format and
 * semantics of the frame. Implementations MUST ignore and discard any frame
 * that has a type that is unknown.
 * </p>
 * </dd>
 * <dt>Flags:</dt>
 * <dd>
 * <p>
 * An 8-bit field reserved for boolean flags specific to the frame type.
 * </p>
 * <p>
 * Flags are assigned semantics specific to the indicated frame type. Flags that
 * have no defined semantics for a particular frame type MUST be ignored and
 * MUST be left unset (0x0) when sending.
 * </p>
 * </dd>
 * <dt>R:</dt>
 * <dd>
 * <p>
 * A reserved 1-bit field. The semantics of this bit are undefined, and the bit
 * MUST remain unset (0x0) when sending and MUST be ignored when receiving.
 * </p>
 * </dd>
 * <dt>Stream Identifier:</dt>
 * <dd>
 * <p>
 * A stream identifier (see <a href="#StreamIdentifiers"
 * title="Stream Identifiers">Section&nbsp;5.1.1</a>) expressed as an unsigned
 * 31-bit integer. The value 0x0 is reserved for frames that are associated with
 * the connection as a whole as opposed to an individual stream.
 * </p>
 * </dd>
 * </dl>
 * 
 */
//http://httpwg.org/specs/rfc7540.html
public class Http2Codec implements ProtocolCodec {

    public static final int     PROTOCOL_HEADER         = 9;
    public static final int     PROTOCOL_PING           = -1;
    public static final int     PROTOCOL_PONG           = -2;
    public static final int     PROTOCOL_PREFACE_HEADER = 24;
    private Http2HeadersEncoder http2HeadersEncoder     = new DefaultHttp2HeadersEncoder();

    private ByteBuf allocate(NioSocketChannel channel, int capacity) {
        return channel.allocator().allocate(capacity);
    }

    @Override
    public ChannelFuture createPINGPacket(NioSocketChannel channel) {
        return null;
    }

    @Override
    public ChannelFuture createPONGPacket(NioSocketChannel channel, ChannelFuture ping) {
        return null;
    }

    @Override
    public ChannelFuture decode(NioSocketChannel channel, ByteBuf buffer) throws IOException {
        Http2Session session = Http2Session.getHttp2Session(channel);
        if (session.isPrefaceRead()) {
            return new Http2PrefaceFuture(allocate(channel, PROTOCOL_PREFACE_HEADER));
        }
        return new Http2FrameHeaderImpl(allocate(channel, PROTOCOL_HEADER));
    }

    @Override
    public void encode(NioSocketChannel channel, ChannelFuture future) throws IOException {
        ByteBufAllocator allocator = channel.allocator();
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
                //          http2HeadersEncoder.encodeHeaders(headers, buffer);
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
        ByteBuf buf = allocator.allocate(length + PROTOCOL_HEADER);
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

    @Override
    public String getProtocolId() {
        return "Http2";
    }

    @Override
    public void initialize(ChannelContext context) {}

}
