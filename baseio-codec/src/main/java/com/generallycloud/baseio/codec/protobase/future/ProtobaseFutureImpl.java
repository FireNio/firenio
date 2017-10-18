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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.component.JsonParameters;
import com.generallycloud.baseio.component.Parameters;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;

/**
 *
 */
public class ProtobaseFutureImpl extends AbstractChannelFuture implements ProtobaseFuture {

    private boolean         body_complete;
    private int             futureId;
    private String          futureName;
    private boolean         header_complete;
    private Parameters      parameters;
    private ByteArrayBuffer writeBinaryBuffer;

    protected int           future_name_length;
    protected int           textLength;

    // for ping & pong
    public ProtobaseFutureImpl(SocketChannelContext context) {
        super(context);
        this.header_complete = true;
        this.body_complete = true;
    }

    public ProtobaseFutureImpl(SocketChannelContext context, int futureId, String futureName) {
        super(context);
        this.futureName = futureName;
        this.futureId = futureId;
    }

    public ProtobaseFutureImpl(SocketChannelContext context, String futureName) {
        this(context, 0, futureName);
    }

    public ProtobaseFutureImpl(SocketChannel channel, ByteBuf buf) {
        super(channel.getContext());
        this.buf = buf;
    }

    private void doBodyComplete(SocketChannel channel, ByteBuf buf) {

        Charset charset = context.getEncoding();

        int offset = buf.offset();

        ByteBuffer memory = buf.nioBuffer();

        memory.limit(offset + future_name_length);

        futureName = StringUtil.decode(charset, memory);

        memory.limit(memory.position() + textLength);

        readText = StringUtil.decode(charset, memory);

        gainBinary(buf, offset);
    }

    private void doHeaderComplete(SocketChannel channel, ByteBuf buf) throws IOException {

        this.future_name_length = buf.getUnsignedByte();

        this.futureId = buf.getInt();

        this.generateHeaderExtend(buf);

        this.textLength = buf.getUnsignedShort();

        this.generateHeaderBinary(buf);

        reallocateBuf(buf);
    }

    protected void generateHeaderBinary(ByteBuf buf) {

    }

    protected void generateHeaderExtend(ByteBuf buf) {

    }

    protected void reallocateBuf(ByteBuf buf) {
        buf.reallocate(future_name_length + textLength);
    }

    protected void gainBinary(ByteBuf buf, int offset) {}

    @Override
    public byte[] getBinary() {
        return null;
    }

    @Override
    public int getBinaryLength() {
        return 0;
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
    public Parameters getParameters() {
        if (parameters == null) {
            parameters = new JsonParameters(getReadText());
        }
        return parameters;
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
        return false;
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {

        ByteBuf buf = this.buf;

        if (!header_complete) {

            buf.read(buffer);

            if (buf.hasRemaining()) {
                return false;
            }

            header_complete = true;

            doHeaderComplete(channel, buf.flip());
        }

        if (!body_complete) {

            buf.read(buffer);

            if (buf.hasRemaining()) {
                return false;
            }

            body_complete = true;

            doBodyComplete(channel, buf.flip());
        }

        return true;
    }

    @Override
    public void setFutureId(int futureId) {
        this.futureId = futureId;
    }

    @Override
    public String toString() {
        return getFutureName() + "@" + getReadText();
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
    public void setFutureName(String futureName) {
        this.futureName = futureName;
    }

}
