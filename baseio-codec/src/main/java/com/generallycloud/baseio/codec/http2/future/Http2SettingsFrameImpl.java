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
package com.generallycloud.baseio.codec.http2.future;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.http2.Http2SocketSession;
import com.generallycloud.baseio.component.SocketChannel;

public class Http2SettingsFrameImpl extends AbstractHttp2Frame implements Http2SettingsFrame {

    private boolean isComplete;

    private long[]  settings;  //FIXME delete

    public Http2SettingsFrameImpl(SocketChannel channel, ByteBuf buf,
            Http2FrameHeader header) {
        super(channel, header);
        this.buf = buf;
    }

    private void doComplete(SocketChannel channel, ByteBuf buf) throws IOException {

        Http2SocketSession session = (Http2SocketSession) channel.getSession();
        
        int settings = buf.limit() / 6;

        for (int i = 0; i < settings; i++) {

            int key = buf.getShort();
            int value = buf.getInt();

            session.setSettings(key, value);
        }

        this.settings = session.getSettings();

        channel.flush(this);
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {

        if (!isComplete) {

            ByteBuf buf = this.buf;

            buf.read(buffer);

            if (buf.hasRemaining()) {
                return false;
            }

            isComplete = true;

            doComplete(channel, buf.flip());
        }

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
