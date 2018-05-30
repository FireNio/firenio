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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.collection.FixedThreadStack;
import com.generallycloud.baseio.component.SelectorEventLoop;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;
import com.generallycloud.baseio.protocol.ChannelFuture;

public class WebSocketFutureImpl extends AbstractChannelFuture implements WebSocketFuture {

    private byte[]  byteArray;
    private boolean eof;
    private boolean hasMask;
    private int     length;
    private int     limit;
    private byte[]  mask;
    private String  readText;
    private boolean remain_data_complete;
    private String  serviceName;
    private int     type;

    public WebSocketFutureImpl() {
        this.type = WebSocketCodec.TYPE_TEXT;
    }

    public WebSocketFutureImpl(SocketChannel channel, ByteBuf buf, int limit) {
        this.limit = limit;
        this.setByteBuf(buf);
        this.setServiceName(channel.getSession());
    }

    @Override
    public byte[] getByteArray() {
        return byteArray;
    }

    @Override
    public String getFutureName() {
        return serviceName;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public String getReadText() {
        return readText;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public boolean isCloseFrame() {
        return OP_CONNECTION_CLOSE_FRAME == type;
    }

    @Override
    public boolean isEof() {
        return eof;
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {
        ByteBuf buf = getByteBuf();
        if (type == 0) {
            buf.read(buffer);
            if (buf.hasRemaining()) {
                return false;
            }
            buf.flip();
            int remain_header_size = 0;
            byte b = buf.getByte();
            eof = (b & 0b10000000) > 0;
            type = (b & 0xF);
            if (type == WebSocketCodec.TYPE_PING) {
                setPING();
            } else if (type == WebSocketCodec.TYPE_PONG) {
                setPONG();
            }
            b = buf.getByte();
            hasMask = (b & 0b10000000) > 0;
            if (hasMask) {
                remain_header_size += 4;
            }
            length = (b & 0x7f);
            if (length < 126) {} else if (length == 126) {
                remain_header_size += 2;
            } else {
                remain_header_size += 4;
            }
            buf.reallocate(remain_header_size);
        }
        if (!remain_data_complete) {
            buf.read(buffer);
            if (buf.hasRemaining()) {
                return false;
            }
            buf.flip();
            if (length < 126) {} else if (length == 126) {
                length = buf.getUnsignedShort();
            } else {
                length = (int) buf.getUnsignedInt();
                if (length < 0) {
                    throw new IOException("too long data length");
                }
            }
            if (hasMask) {
                mask = buf.getBytes();
            }
            buf.reallocate(length, limit);
            remain_data_complete = true;
        }
        buf.read(buffer);
        if (buf.hasRemaining()) {
            return false;
        }
        buf.flip();
        byte[] array = buf.getBytes();
        if (hasMask) {
            byte[] mask = this.mask;
            int length = array.length;
            for (int i = 0; i < length; i++) {
                array[i] = (byte) (array[i] ^ mask[i % 4]);
            }
        }
        this.byteArray = array;
        if (type == WebSocketCodec.TYPE_BINARY) {
            // FIXME 处理binary
        } else {
            this.readText = new String(array, channel.getEncoding());
        }
        return true;
    }

    @Override
    public ChannelFuture setPING() {
        this.type = WebSocketCodec.TYPE_PING;
        return super.setPING();
    }

    @Override
    public ChannelFuture setPONG() {
        this.type = WebSocketCodec.TYPE_PONG;
        return super.setPONG();
    }

    protected void setServiceName(SocketSession session) {
        this.serviceName = (String) session.getAttribute(SESSION_KEY_SERVICE_NAME);
    }

    protected void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return getReadText();
    }

    @Override
    public void release(SelectorEventLoop eventLoop) {
        super.release(eventLoop);
        //FIXME ..final statck is null or not null
        if (WebSocketCodec.WS_PROTOCOL_CODEC.getFutureStackSize() == 0) {
            return;
        }
        FixedThreadStack<WebSocketFutureImpl> stack = (FixedThreadStack<WebSocketFutureImpl>) eventLoop
                .getAttribute(WebSocketCodec.FUTURE_STACK_KEY);
        if (stack != null) {
            stack.push(this);
        }
    }

    protected WebSocketFutureImpl reset(SocketChannel channel, ByteBuf buf, int limit) {
        this.byteArray = null;
        this.eof = false;
        this.hasMask = false;
        this.length = 0;
        this.mask = null;
        this.readText = null;
        this.remain_data_complete = false;
        this.type = 0;

        this.limit = limit;
        this.setByteBuf(buf);
        this.setServiceName(channel.getSession());

        super.reset();
        return this;
    }

}
