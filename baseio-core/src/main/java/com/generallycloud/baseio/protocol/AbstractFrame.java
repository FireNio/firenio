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

import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;

public abstract class AbstractFrame implements Frame {

    private static final byte TYPE_PING   = 1;
    private static final byte TYPE_PONG   = 2;
    private static final byte TYPE_SILENT = 0;

    private byte    frameType;
    private boolean isTyped;
    private byte[]  writeBuffer;
    private int     writeSize;

    @Override
    public byte[] getWriteBuffer() {
        return writeBuffer;
    }

    @Override
    public int getWriteSize() {
        return writeSize;
    }

    @Override
    public boolean isPing() {
        return frameType == TYPE_PING;
    }

    @Override
    public boolean isPong() {
        return frameType == TYPE_PONG;
    }

    @Override
    public boolean isSilent() {
        return frameType == TYPE_SILENT;
    }
    
    @Override
    public boolean isType(byte type) {
        return frameType == type;
    }

    @Override
    public boolean isTyped() {
        return isTyped;
    }

    protected Frame reset() {
        this.frameType = 0;
        this.isTyped = false;
        this.writeSize = 0;
        this.writeBuffer = null;
        return this;
    }

    @Override
    public Frame setPing() {
        this.frameType = TYPE_PING;
        this.isTyped = true;
        return this;
    }

    @Override
    public Frame setPong() {
        this.frameType = TYPE_PONG;
        this.isTyped = true;
        return this;
    }

    @Override
    public void setSilent() {
        this.frameType = TYPE_SILENT;
        this.isTyped = true;
    }

    @Override
    public void setType(byte type) {
        this.frameType = type;
        this.isTyped = true;
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
        write(text, context.getCharset());
    }

    @Override
    public void write(String text, Charset charset) {
        write(text.getBytes(charset));
    }

    @Override
    public void write(String text, NioSocketChannel ch) {
        write(text, ch.getContext());
    }

}
