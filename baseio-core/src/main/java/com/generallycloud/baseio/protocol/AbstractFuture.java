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
package com.generallycloud.baseio.protocol;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioEventLoop;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.concurrent.Linkable;

public abstract class AbstractFuture implements Future {

    //FIXME isX 使用 byte & x ?
    private ByteBuf  buf        = EmptyByteBuf.get();
    private long     bufReleaseVersion;
    private boolean  flushed;
    private boolean  isHeartbeat;
    private boolean  isNeedSsl;
    private boolean  isPING;
    private boolean  isSilent;
    private boolean  isValidate = true;
    private Linkable next;
    protected byte[] writeBuffer;
    protected int    writeSize;

    protected ByteBuf allocate(NioSocketChannel channel, int capacity) {
        return channel.allocator().allocate(capacity);
    }

    protected ByteBuf allocate(NioSocketChannel channel, int capacity, int maxLimit) {
        return channel.allocator().allocate(capacity, maxLimit);
    }

    @Override
    public Future duplicate() {
        return new DuplicateFuture(buf.duplicate(), this);
    }

    @Override
    public final Future flush() {
        flushed = true;
        return this;
    }

    @Override
    public boolean flushed() {
        return flushed;
    }

    @Override
    public ByteBuf getByteBuf() {
        return buf;
    }

    @Override
    public int getByteBufLimit() {
        return buf.limit();
    }

    @Override
    public Linkable getNext() {
        return next;
    }

    @Override
    public byte[] getWriteBuffer() {
        return writeBuffer;
    }

    @Override
    public int getWriteSize() {
        return writeSize;
    }

    @Override
    public boolean isHeartbeat() {
        return isHeartbeat;
    }

    @Override
    public boolean isNeedSsl() {
        return isNeedSsl;
    }

    @Override
    public boolean isPING() {
        return isHeartbeat && isPING;
    }

    @Override
    public boolean isPONG() {
        return isHeartbeat && !isPING;
    }

    @Override
    public boolean isReleased() {
        return buf.isReleased();
    }

    @Override
    public boolean isSilent() {
        return isSilent;
    }

    @Override
    public boolean isValidate() {
        return isValidate;
    }

    @Override
    public boolean isWriteCompleted() {
        return !buf.hasRemaining();
    }

    @Override
    public void release(NioEventLoop eventLoop) {
        ReleaseUtil.release(buf, bufReleaseVersion);
    }

    protected Future reset() {
        this.flushed = false;
        this.isHeartbeat = false;
        this.isNeedSsl = false;
        this.isSilent = false;
        this.next = null;
        this.writeSize = 0;
        this.bufReleaseVersion = 0;
        return this;
    }

    @Override
    public void setByteBuf(ByteBuf buf) {
        buf.nioBuffer();
        this.buf = buf;
        this.bufReleaseVersion = buf.getReleaseVersion();
    }

    @Override
    public void setHeartbeat(boolean isPing) {
        this.isPING = isPing;
        this.isHeartbeat = true;
    }

    @Override
    public void setNeedSsl(boolean needSsl) {
        this.isNeedSsl = needSsl;
    }

    @Override
    public void setNext(Linkable next) {
        this.next = next;
    }

    @Override
    public Future setPING() {
        this.isPING = true;
        this.isHeartbeat = true;
        return this;
    }

    @Override
    public Future setPONG() {
        this.isPING = false;
        this.isHeartbeat = true;
        return this;
    }

    @Override
    public void setSilent(boolean isSilent) {
        this.flushed = isSilent;
        this.isSilent = isSilent;
    }

    @Override
    public void setValidate(boolean validate) {
        this.isValidate = validate;
    }

    @Override
    public void write(byte b) {
        if (writeBuffer == null) {
            writeBuffer = new byte[256];
        }
        int newcount = writeSize + 1;
        if (newcount > writeBuffer.length) {
            writeBuffer = Arrays.copyOf(writeBuffer, writeBuffer.length << 1);
        }
        writeBuffer[writeSize] = b;
        writeSize = newcount;
    }

    @Override
    public void write(byte[] bytes) {
        write(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] bytes, int off, int len) {
        if (writeBuffer == null) {
            if ((len - off) != bytes.length) {
                writeBuffer = new byte[len];
                writeSize = len;
                System.arraycopy(bytes, off, writeBuffer, 0, len);
                return;
            }
            writeBuffer = bytes;
            writeSize = len;
            return;
        }
        int newcount = writeSize + len;
        if (newcount > writeBuffer.length) {
            writeBuffer = Arrays.copyOf(writeBuffer, Math.max(writeBuffer.length << 1, newcount));
        }
        System.arraycopy(bytes, off, writeBuffer, writeSize, len);
        writeSize = newcount;
    }

    @Override
    public void write(String text, ChannelContext context) {
        write(text, context.getEncoding());
    }

    @Override
    public void write(String text, Charset charset) {
        write(text.getBytes(charset));
    }

    @Override
    public void write(String text, NioSocketChannel channel) {
        write(text, channel.getContext());
    }

}
