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

import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.component.SocketChannelContext;

public abstract class AbstractFuture implements Future {

    protected boolean              flushed;
    protected String               readText;
    protected SocketChannelContext context;
    protected ByteArrayBuffer      writeBuffer;

    protected AbstractFuture(SocketChannelContext context) {
        this.context = context;
    }

    @Override
    public boolean flushed() {
        return flushed;
    }

    @Override
    public SocketChannelContext getContext() {
        return context;
    }

    @Override
    public String getReadText() {
        return readText;
    }

    @Override
    public void write(String text) {
        write(text, context.getEncoding());
    }

    @Override
    public String toString() {
        return getReadText();
    }

    @Override
    public ByteArrayBuffer getWriteBuffer() {
        return writeBuffer;
    }

    @Override
    public void write(String text, Charset charset) {
        write(text.getBytes(charset));
    }

    @Override
    public void write(byte b) {
        if (writeBuffer == null) {
            writeBuffer = new ByteArrayBuffer();
        }
        writeBuffer.write(b);
    }

    @Override
    public void write(byte[] bytes) {
        write(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] bytes, int off, int len) {
        if (writeBuffer == null) {
            if (off != 0) {
                byte[] copy = new byte[len - off];
                System.arraycopy(bytes, off, copy, 0, len);
                writeBuffer = new ByteArrayBuffer(copy, len);
                return;
            }
            writeBuffer = new ByteArrayBuffer(bytes, len);
            return;
        }
        writeBuffer.write(bytes, off, len);
    }

}
