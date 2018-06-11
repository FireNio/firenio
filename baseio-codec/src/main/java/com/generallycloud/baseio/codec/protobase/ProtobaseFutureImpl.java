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
package com.generallycloud.baseio.codec.protobase;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolException;

/**
 *
 */
public class ProtobaseFutureImpl extends AbstractChannelFuture implements ProtobaseFuture {

    private int     binaryLengthLimit;
    private byte[]  binaryReadBuffer;
    private int     binaryReadSize;
    private byte[]  binaryWriteBuffer;
    private int     binaryWriteSize;
    private int     futureId;
    private String  futureName;
    private boolean isBroadcast;
    private String  readText;
    private int     sessionId;
    private int     textLength;
    private int     textLengthLimit;

    // for ping & pong
    public ProtobaseFutureImpl() {}

    public ProtobaseFutureImpl(ByteBuf buf) {
        this.setByteBuf(buf);
    }

    public ProtobaseFutureImpl(int futureId, String futureName) {
        this.futureName = futureName;
        this.futureId = futureId;
    }

    public ProtobaseFutureImpl(String futureName) {
        this(0, futureName);
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
    public byte[] getReadBinary() {
        return binaryReadBuffer;
    }

    @Override
    public int getReadBinarySize() {
        return binaryReadSize;
    }

    @Override
    public String getReadText() {
        return readText;
    }

    @Override
    public int getSessionId() {
        return sessionId;
    }

    @Override
    public int getSessionKey() {
        return sessionId;
    }

    @Override
    public int getTextLength() {
        return textLength;
    }

    @Override
    public byte[] getWriteBinary() {
        return binaryWriteBuffer;
    }

    @Override
    public int getWriteBinarySize() {
        return binaryWriteSize;
    }

    @Override
    public boolean hasReadBinary() {
        return binaryReadSize > 0;
    }

    @Override
    public boolean isBroadcast() {
        return isBroadcast;
    }
    
    private void setHeartbeat(int len) {
        if (len == ProtobaseCodec.PROTOCOL_PING) {
            setPING();
        } else if (len == ProtobaseCodec.PROTOCOL_PONG) {
            setPONG();
        } else {
            throw new ProtocolException("illegal length:" + len);
        }
    }

    @Override
    public boolean read(NioSocketChannel channel, ByteBuf src) throws IOException {
        if (src.remaining() < 4) {
            return false;
        }
        int len = src.getInt();
        if (len < 0) {
            setHeartbeat(len);
            return true;
        }
        if (len > src.remaining()) {
            src.position(src.position() - 4);
            return false;
        }
        byte h1 = src.getByte();
        int fnLen = src.getUnsignedByte();
        isBroadcast = ((h1 & 0b10000000) != 0);
        boolean hasText = ((h1 & 0b00010000) != 0);
        boolean hasBinary = ((h1 & 0b00001000) != 0);
        Charset charset = channel.getEncoding();
        src.markL();
        src.limit(src.position() + fnLen);
        futureName = StringUtil.decode(charset, src.nioBuffer());
        src.reverse();
        src.resetL();
        if (((h1 & 0b01000000) != 0)) {
            futureId = src.getInt();
        }
        if (((h1 & 0b00100000) != 0)) {
            sessionId = src.getInt();
        }
        int textLen = 0;
        int binaryLen = 0;
        if (hasText) {
            textLen = src.getInt();
        }
        if (hasBinary) {
            binaryLen = src.getInt();
            binaryReadSize = binaryLen;
        }
        if (hasText) {
            src.markL();
            src.limit(src.position() + textLen);
            readText = StringUtil.decode(charset, src.nioBuffer());
            src.reverse();
            src.resetL();
        }
        if (hasBinary) {
            src.markL();
            src.limit(src.position() + binaryLen);
            this.binaryReadBuffer = src.getBytes();
            src.reverse();
            src.resetL();
        }
        return true;
    }

    @Override
    public void setBroadcast(boolean broadcast) {
        this.isBroadcast = broadcast;
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
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return getFutureName() + "@" + getReadText();
    }

    @Override
    public void writeBinary(byte b) {
        if (binaryWriteBuffer == null) {
            binaryWriteBuffer = new byte[256];
        }
        int newcount = binaryWriteSize + 1;
        if (newcount > binaryWriteBuffer.length) {
            binaryWriteBuffer = Arrays.copyOf(binaryWriteBuffer, binaryWriteBuffer.length << 1);
        }
        binaryWriteBuffer[binaryWriteSize] = b;
        binaryWriteSize = newcount;
    }

    @Override
    public void writeBinary(byte[] bytes) {
        writeBinary(bytes, 0, bytes.length);
    }

    @Override
    public void writeBinary(byte[] bytes, int off, int len) {
        if (binaryWriteBuffer == null) {
            if ((len - off) != bytes.length) {
                binaryWriteBuffer = new byte[len];
                binaryWriteSize = len;
                System.arraycopy(bytes, off, binaryWriteBuffer, 0, len);
                return;
            }
            binaryWriteBuffer = bytes;
            binaryWriteSize = len;
            return;
        }
        int newcount = binaryWriteSize + len;
        if (newcount > binaryWriteBuffer.length) {
            binaryWriteBuffer = Arrays.copyOf(binaryWriteBuffer,
                    Math.max(binaryWriteBuffer.length << 1, newcount));
        }
        System.arraycopy(bytes, off, binaryWriteBuffer, binaryWriteSize, len);
        binaryWriteSize = newcount;
    }

}
