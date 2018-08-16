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
package com.generallycloud.baseio.codec.http2.frame;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.MathUtil;
import com.generallycloud.baseio.component.NioEventLoop;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.AbstractFrame;

public class Http2FrameHeaderImpl extends AbstractFrame implements Http2FrameHeader {

    private boolean          header_complete;
    private byte             flags;
    private int              streamIdentifier;
    private SocketHttp2Frame frame;
    private ByteBuf          buf;

    public Http2FrameHeaderImpl(ByteBuf buf) {
        this.buf = buf;
    }

    public Http2FrameHeaderImpl() {}

    private void doHeaderComplete(NioSocketChannel ch, ByteBuf buf) {
        byte b0 = buf.getByte();
        byte b1 = buf.getByte();
        byte b2 = buf.getByte();
        int length = ((b0 & 0xff) << 8 * 2) | ((b1 & 0xff) << 8 * 1) | ((b2 & 0xff) << 8 * 0);
        int type = buf.getUnsignedByte();
        this.flags = buf.getByte();
        this.streamIdentifier = MathUtil.int2int31(buf.getInt());
        this.frame = genFrame(ch, type, length);
    }

    @Override
    public boolean read(NioSocketChannel ch, ByteBuf buffer) throws IOException {
        ByteBuf buf = this.buf;
        if (!header_complete) {
            buf.read(buffer);
            if (buf.hasRemaining()) {
                return false;
            }
            header_complete = true;
            doHeaderComplete(ch, buf.flip());
        }
        return frame.read(ch, buffer);
    }

    @Override
    public byte getFlags() {
        return flags;
    }

    @Override
    public boolean isSilent() {
        return frame.isSilent();
    }

    @Override
    public Http2FrameType getHttp2FrameType() {
        return frame.getHttp2FrameType();
    }

    @Override
    public int getStreamIdentifier() {
        return streamIdentifier;
    }

    @Override
    public Http2Frame getFrame() {
        return frame;
    }

    private SocketHttp2Frame genFrame(NioSocketChannel ch, Http2FrameType type, int length) {
        ByteBufAllocator allocator = ch.alloc();
        switch (type) {
            case FRAME_TYPE_CONTINUATION:
                break;
            case FRAME_TYPE_DATA:
                break;
            case FRAME_TYPE_GOAWAY:
                break;
            case FRAME_TYPE_HEADERS:
                return new Http2HeadersFrameImpl(allocator.allocate(length), this);
            case FRAME_TYPE_PING:
                break;
            case FRAME_TYPE_PRIORITY:
                break;
            case FRAME_TYPE_PUSH_PROMISE:
                break;
            case FRAME_TYPE_RST_STREAM:
                break;
            case FRAME_TYPE_SETTINGS:
                return new Http2SettingsFrameImpl(allocator.allocate(length), this);
            case FRAME_TYPE_WINDOW_UPDATE:
                return new Http2WindowUpdateFrameImpl(allocator.allocate(length), this);
            default:
                break;
        }
        throw new IllegalArgumentException(type.toString());
    }

    private SocketHttp2Frame genFrame(NioSocketChannel ch, int type, int length) {
        return genFrame(ch, Http2FrameType.getValue(type), length);
    }

    @Override
    public void release(NioEventLoop eventLoop) {
        super.release(eventLoop);
        frame.release(eventLoop);
    }

}
