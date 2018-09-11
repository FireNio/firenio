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
package com.generallycloud.baseio.container.http11;

import java.io.IOException;
import java.nio.charset.Charset;

import com.generallycloud.baseio.collection.AttributesImpl;
import com.generallycloud.baseio.common.UUIDGenerator;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;

public class DefaultHttpSession extends AttributesImpl implements HttpSession {

    private long               createTime     = System.currentTimeMillis();
    private NioSocketChannel      ch;
    private long               lastAccessTime = createTime;
    private String             sessionId;
    private HttpFrameAcceptor context;

    protected DefaultHttpSession(HttpFrameAcceptor context, NioSocketChannel ioSession) {
        this.context = context;
        this.ch = ioSession;
        this.sessionId = UUIDGenerator.random();
    }

    protected DefaultHttpSession(HttpFrameAcceptor context, NioSocketChannel ioSession,
            String sessionId) {
        this.context = context;
        this.ch = ioSession;
        this.sessionId = sessionId;
    }

    @Override
    public void active(NioSocketChannel ioSession) {
        this.ch = ioSession;
        this.lastAccessTime = System.currentTimeMillis();
    }

    @Override
    public void flush(Frame frame) throws IOException {
        ch.flush(frame);
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public NioSocketChannel getChannel() {
        return ch;
    }

    @Override
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public Charset getEncoding() {
        return ch.getCharset();
    }

    @Override
    public boolean isValidate() {
        return System.currentTimeMillis() - lastAccessTime < 1000 * 60 * 30;
    }

    @Override
    public HttpFrameAcceptor getContext() {
        return context;
    }
}
