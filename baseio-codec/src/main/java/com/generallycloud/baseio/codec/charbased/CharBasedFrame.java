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
package com.generallycloud.baseio.codec.charbased;

import java.io.IOException;
import java.nio.charset.Charset;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.component.ByteArrayOutputStream;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.AbstractFrame;
import com.generallycloud.baseio.protocol.TextFrame;

public class CharBasedFrame extends AbstractFrame implements TextFrame {

    private ByteArrayOutputStream cache = new ByteArrayOutputStream();
    private int                   limit;
    private String                readText;
    private byte                  splitor;

    public CharBasedFrame() {}

    CharBasedFrame(int limit, byte splitor) {
        this.limit = limit;
        this.splitor = splitor;
    }

    public ByteArrayOutputStream getLineOutputStream() {
        return cache;
    }

    @Override
    public String getReadText() {
        return readText;
    }

    @Override
    public boolean read(NioSocketChannel ch, ByteBuf buffer) throws IOException {
        ByteArrayOutputStream cache = this.cache;
        Charset charset = ch.getCharset();
        for (; buffer.hasRemaining();) {
            byte b = buffer.getByte();
            if (b == splitor) {
                this.readText = cache.toString(charset);
                return true;
            }
            cache.write(b);
            if (cache.size() > limit) {
                throw new IOException("max length " + limit);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getReadText();
    }

}
