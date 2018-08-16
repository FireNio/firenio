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
package com.generallycloud.baseio.codec.http2.frame;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.http2.Http2Session;
import com.generallycloud.baseio.component.NioSocketChannel;

public class Http2SettingsFrameImpl extends AbstractHttp2Frame implements Http2SettingsFrame {

    private long[] settings; //FIXME delete

    public Http2SettingsFrameImpl(ByteBuf buf, Http2FrameHeader header) {
        super(header);
        this.setByteBuf(buf);
    }

    private void doComplete(NioSocketChannel ch, ByteBuf buf) throws IOException {
        Http2Session session = Http2Session.getHttp2Session(ch);
        int settings = buf.limit() / 6;
        for (int i = 0; i < settings; i++) {
            int key = buf.getShort();
            int value = buf.getInt();
            session.setSettings(key, value);
        }
        this.settings = session.getSettings();
        ch.flush(this);
    }

    @Override
    public boolean read(NioSocketChannel ch, ByteBuf buffer) throws IOException {
        ByteBuf buf = getByteBuf();
        buf.read(buffer);
        if (buf.hasRemaining()) {
            return false;
        }
        doComplete(ch, buf.flip());
        return true;
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public Http2FrameType getHttp2FrameType() {
        return Http2FrameType.FRAME_TYPE_SETTINGS;
    }

    @Override
    public long[] getSettings() {
        return settings;
    }

}
