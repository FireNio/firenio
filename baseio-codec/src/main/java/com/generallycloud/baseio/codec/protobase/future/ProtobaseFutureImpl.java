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
package com.generallycloud.baseio.codec.protobase.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.generallycloud.baseio.balance.BalanceFuture;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;

/**
 *
 */
public class ProtobaseFutureImpl extends AbstractChannelFuture implements ProtobaseFuture {

    private byte[]          binary;
    private int             binaryLength;
    private int             binaryLengthLimit;
    private boolean         body_complete;
    private int             futureId;
    private String          futureName;
    private int             hashCode;
    private byte            futureNameLength;
    private boolean         header_complete;
    private int             sessionId;
    private boolean         isBroadcast;
    private int             textLength;
    private int             textLengthLimit;
    private ByteArrayBuffer writeBinaryBuffer;

    public ProtobaseFutureImpl(SocketChannel channel, ByteBuf buf) {
        super(channel.getContext());
        this.buf = buf;
    }

    // for ping & pong
    public ProtobaseFutureImpl(SocketChannelContext context) {
        super(context);
        this.header_complete = true;
        this.body_complete = true;
    }

    public ProtobaseFutureImpl(SocketChannelContext context, String futureName) {
        this(context, 0, futureName);
    }

    public ProtobaseFutureImpl(SocketChannelContext context, int futureId, String futureName) {
        super(context);
        this.futureName = futureName;
        this.futureId = futureId;
    }

    @Override
    public byte[] getBinary() {
        return binary;
    }

    @Override
    public int getBinaryLength() {
        return binaryLength;
    }

    @Override
    public int getFutureId() {
        return futureId;
    }

    @Override
    public String getFutureName() {
        return futureName;
    }

    @Override
    public int getHashCode() {
        return hashCode;
    }

    @Override
    public int getSessionId() {
        return sessionId;
    }

    @Override
    public int getTextLength() {
        return textLength;
    }

    @Override
    public ByteArrayBuffer getWriteBinaryBuffer() {
        return writeBinaryBuffer;
    }

    @Override
    public boolean hasBinary() {
        return binaryLength > 0;
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {
        ByteBuf buf = this.buf;
        if (!header_complete) {
            buf.read(buffer);
            if (buf.hasRemaining()) {
                return false;
            }
            if (futureNameLength == 0) {
                int nextLen = 4;
                byte h1 = buf.getByte(0);
                int type = h1 & 0b11000000;
                if (type > 0b01000000) {
                    setHeartbeat(type == 0b10000000);
                    return true;
                }
                isBroadcast = ((h1 & 0b00100000) != 0);
                if ((h1 & 0b00010000) != 0) {
                    futureId = -1;
                    nextLen+=4;
                }
                if ((h1 & 0b00001000) != 0) {
                    sessionId = -1;
                    nextLen+=4;
                }
                if ((h1 & 0b00000100) != 0) {
                    hashCode = -1;
                    nextLen+=4;
                }
                if ((h1 & 0b00000010) != 0) {
                    binaryLength = -1;
                    nextLen+=4;
                }
                futureNameLength = buf.getByte(1);
                if (futureNameLength < 1) {
                    throw new IOException("futureNameLength < 1");
                }
                nextLen += futureNameLength;
                buf.reallocate(nextLen);
                return read(channel, buffer);
            }
            buf.flip();
            textLength = buf.getInt();
            if (futureId == -1) {
                futureId = buf.getInt();
            }
            if (sessionId == -1) {
                sessionId = buf.getInt();
            }
            if (hashCode == -1) {
                hashCode = buf.getInt();
            }
            if (binaryLength == -1) {
                binaryLength = buf.getInt();
            }
            Charset charset = context.getEncoding();
            ByteBuffer memory = buf.nioBuffer();
            futureName = StringUtil.decode(charset, memory);
            buf.reallocate(textLength+binaryLength);
            header_complete = true;
        }
        if (!body_complete) {
            buf.read(buffer);
            if (buf.hasRemaining()) {
                return false;
            }
            body_complete = true;
            if (textLength > 0) {
                buf.flip();
                buf.markPL();
                buf.limit(textLength);
                Charset charset = context.getEncoding();
                ByteBuffer memory = buf.nioBuffer();
                readText = StringUtil.decode(charset, memory);
                buf.reset();
                buf.skipBytes(textLength);
            }
            if (binaryLength > 0) {
                this.binary = buf.getBytes();
            }
        }
        return true;
    }

    @Override
    public void setFutureId(int futureId) {
        this.futureId = futureId;
    }

    @Override
    public void setFutureName(String futureName) {
        this.futureName = futureName;
    }

    @Override
    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return getFutureName() + "@" + getReadText();
    }

    @Override
    public BalanceFuture translate() {
        String text = getReadText();
        if (StringUtil.isNullOrBlank(text)) {
            return this;
        }
        write(text);
        return this;
    }

    @Override
    public void writeBinary(byte b) {
        if (writeBinaryBuffer == null) {
            writeBinaryBuffer = new ByteArrayBuffer();
        }
        writeBinaryBuffer.write(b);
    }

    @Override
    public void writeBinary(byte[] bytes) {
        if (bytes == null) {
            return;
        }
        writeBinary(bytes, 0, bytes.length);
    }

    @Override
    public void writeBinary(byte[] bytes, int offset, int length) {
        if (writeBinaryBuffer == null) {
            if (offset != 0) {
                byte[] copy = new byte[length - offset];
                System.arraycopy(bytes, offset, copy, 0, length);
                writeBinaryBuffer = new ByteArrayBuffer(copy, length);
                return;
            }
            writeBinaryBuffer = new ByteArrayBuffer(bytes, length);
            return;
        }
        writeBinaryBuffer.write(bytes, offset, length);
    }

    @Override
    public int getSessionKey() {
        return sessionId;
    }

    @Override
    public boolean isBroadcast() {
        return isBroadcast;
    }

    @Override
    public void setBroadcast(boolean broadcast) {
        this.isBroadcast = broadcast;
    }

}
