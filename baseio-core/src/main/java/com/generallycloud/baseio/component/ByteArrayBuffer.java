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
package com.generallycloud.baseio.component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.generallycloud.baseio.common.Encoding;

//FIXME 用到这里的检查是否需要实例化
public class ByteArrayBuffer extends OutputStream {

    private byte cache[];
    private int  count;

    public ByteArrayBuffer() {
        this(128);
    }

    public ByteArrayBuffer(byte[] buffer) {
        this(buffer, 0);
    }

    public ByteArrayBuffer(byte[] buffer, int count) {
        this.cache = buffer;
        this.count = count;
    }

    public ByteArrayBuffer(int size) {
        this(size, 0);
    }

    public ByteArrayBuffer(int size, int count) {
        this(new byte[size], count);
    }

    public byte[] array() {
        return cache;
    }

    public void reset() {
        count = 0;
    }

    public int size() {
        return count;
    }

    @Override
    public String toString() {
        return toString(Encoding.UTF8);
    }

    public String toString(Charset charset) {
        if (count == 0) {
            return null;
        }
        return new String(cache, 0, count,charset);
    }

    @Override
    public void write(byte bytes[], int offset, int length) {
        int newcount = count + length;
        if (newcount > cache.length) {
            cache = Arrays.copyOf(cache, Math.max(cache.length << 1, newcount));
        }
        System.arraycopy(bytes, offset, cache, count, length);
        count = newcount;
    }

    @Override
    public void write(byte[] bytes) {
        write(bytes, 0, bytes.length);
    }

    @Override
    public void write(int b) {
        int newcount = count + 1;
        if (newcount > cache.length) {
            cache = Arrays.copyOf(cache, cache.length << 1);
        }
        cache[count] = (byte) b;
        count = newcount;
    }

    public void write2OutputStream(OutputStream out) throws IOException {
        out.write(cache, 0, count);
    }

}
