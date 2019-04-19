/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.codec.http2;

import com.firenio.component.Channel;

public class Http2Session {

    private Channel ch;

    private Http2Headers http2Headers = new Http2HeadersImpl();

    private boolean prefaceRead = true;

    private long[] settings = new long[]{0, 4096, 1, 128, 65535, 16384, 0};

    public Channel getChannel() {
        return ch;
    }

    public Http2Headers getHttp2Headers() {
        return http2Headers;
    }

    public long[] getSettings() {
        return settings;
    }

    public long getSettings(int i) {
        return settings[i];
    }

    public boolean isPrefaceRead() {
        return prefaceRead;
    }

    public void setChannel(Channel ch) {
        this.ch = ch;
    }

    public void setPrefaceRead(boolean prefaceRead) {
        this.prefaceRead = prefaceRead;
    }

    public void setSettings(int key, long value) {
        settings[key] = value;
    }

}
