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
package com.generallycloud.baseio.codec.fixedlength;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.AbstractFrame;
import com.generallycloud.baseio.protocol.TextFrame;

public class FixedLengthFrame extends AbstractFrame implements TextFrame {

    private int    limit;
    private String readText;

    public FixedLengthFrame() {}

    public FixedLengthFrame(int limit) {
        this.limit = limit;
    }

    @Override
    public String getReadText() {
        return readText;
    }

    @Override
    public boolean read(NioSocketChannel ch, ByteBuf src) throws IOException {
        if (src.remaining() < 4) {
            return false;
        }
        int len = src.getInt();
        if (len < 0) {
            setHeartbeat(len);
            return true;
        }
        if (len > limit) {
            throw new IOException("over limit:" + len);
        }
        if (len > src.remaining()) {
            src.skip(-4);
            return false;
        }
        src.markL();
        src.limit(src.position() + len);
        readText = StringUtil.decode(ch.getCharset(), src.nioBuffer());
        src.reverse();
        src.resetL();
        return true;
    }

    private void setHeartbeat(int len) throws IOException {
        if (len == FixedLengthCodec.PROTOCOL_PING) {
            setPing();
        } else if (len == FixedLengthCodec.PROTOCOL_PONG) {
            setPong();
        } else {
            throw new IOException("illegal length:" + len);
        }
    }

    @Override
    public String toString() {
        return getReadText();
    }

}
