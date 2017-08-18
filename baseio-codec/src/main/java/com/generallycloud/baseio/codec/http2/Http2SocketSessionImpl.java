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
package com.generallycloud.baseio.codec.http2;

import com.generallycloud.baseio.codec.http2.hpack.Http2Headers;
import com.generallycloud.baseio.codec.http2.hpack.Http2HeadersImpl;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.UnsafeSocketSessionImpl;

public class Http2SocketSessionImpl extends UnsafeSocketSessionImpl implements Http2SocketSession {

    public Http2SocketSessionImpl(SocketChannel channel) {
        super(channel);
    }

    private boolean      prefaceRead  = true;

    private long[]       settings     = new long[] { 0, 4096, 1, 128, 65535, 16384, 0 };

    private Http2Headers http2Headers = new Http2HeadersImpl();

    @Override
    public long getSettings(int i) {
        return settings[i];
    }

    @Override
    public void setSettings(int key, long value) {
        settings[key] = value;
    }

    @Override
    public long[] getSettings() {
        return settings;
    }

    @Override
    public Http2Headers getHttp2Headers() {
        return http2Headers;
    }

    @Override
    public boolean isPrefaceRead() {
        return prefaceRead;
    }

    @Override
    public void setPrefaceRead(boolean prefaceRead) {
        this.prefaceRead = prefaceRead;
    }

}
