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
import com.generallycloud.baseio.codec.http2.future.Http2FrameHeaderImpl;
import com.generallycloud.baseio.codec.http2.future.Http2PrefaceFuture;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;

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
public class Http2ProtocolDecoder implements ProtocolDecoder {

    public static final int PROTOCOL_PREFACE_HEADER = 24;

    public static final int PROTOCOL_HEADER         = 9;

    public static final int PROTOCOL_PING           = -1;

    public static final int PROTOCOL_PONG           = -2;

    @Override
    public ChannelFuture decode(SocketChannel channel, ByteBuf buffer) throws IOException {

        Http2SocketSession http2UnsafeSession = (Http2SocketSession) channel.getSession();

        SocketChannelContext context = channel.getContext();

        if (http2UnsafeSession.isPrefaceRead()) {
            return new Http2PrefaceFuture(context, allocate(channel, PROTOCOL_PREFACE_HEADER));
        }
        return new Http2FrameHeaderImpl(channel, allocate(channel, PROTOCOL_HEADER));
    }

    private ByteBuf allocate(SocketChannel channel, int capacity) {
        return channel.getByteBufAllocator().allocate(capacity);
    }

}
