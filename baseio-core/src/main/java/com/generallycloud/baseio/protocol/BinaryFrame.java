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

import java.util.Arrays;

public abstract class BinaryFrame extends AbstractFrame {

    private byte[] binaryWriteBuffer;
    private int    binaryWriteSize;

    public byte[] getWriteBinary() {
        return binaryWriteBuffer;
    }

    public int getWriteBinarySize() {
        return binaryWriteSize;
    }

    @Override
    protected Frame reset() {
        this.binaryWriteSize = 0;
        this.binaryWriteBuffer = null;
        return super.reset();
    }

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

    public void writeBinary(byte[] bytes) {
        writeBinary(bytes, 0, bytes.length);
    }

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
