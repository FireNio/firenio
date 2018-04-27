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

public abstract class AbstractFuture implements Future {

    protected byte[] writeBuffer;
    protected int    writeSize;

    @Override
    public byte[] getWriteBuffer() {
        return writeBuffer;
    }

    @Override
    public int getWriteSize() {
        return writeSize;
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
        writeBuffer[writeSize] = (byte) b;
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
    public void write(String text) {
        write(text.getBytes());
    }

    @Override
    public void write(String text, Charset charset) {
        write(text.getBytes(charset));
    }

}
