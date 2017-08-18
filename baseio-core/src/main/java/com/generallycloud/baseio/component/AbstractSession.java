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

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.CloseUtil;

public abstract class AbstractSession implements Session {

    protected Object                  attachment;
    protected HashMap<Object, Object> attributes = new HashMap<>();

    protected abstract Channel getChannel();

    @Override
    public void active() {
        getChannel().active();
    }

    @Override
    public void clearAttributes() {
        attributes.clear();
    }

    @Override
    public Object getAttachment() {
        return attachment;
    }

    @Override
    public Object getAttribute(Object key) {
        return attributes.get(key);
    }

    @Override
    public HashMap<Object, Object> getAttributes() {
        return attributes;
    }

    @Override
    public long getCreationTime() {
        return getChannel().getCreationTime();
    }

    @Override
    public Charset getEncoding() {
        return getContext().getEncoding();
    }

    @Override
    public long getLastAccessTime() {
        return getChannel().getLastAccessTime();
    }

    @Override
    public String getLocalAddr() {
        return getChannel().getLocalAddr();
    }

    @Override
    public String getLocalHost() {
        return getChannel().getLocalHost();
    }

    @Override
    public int getLocalPort() {
        return getChannel().getLocalPort();
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return getChannel().getLocalSocketAddress();
    }

    @Override
    public String getRemoteAddr() {
        return getChannel().getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return getChannel().getRemoteHost();
    }

    @Override
    public int getRemotePort() {
        return getChannel().getRemotePort();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return getChannel().getRemoteSocketAddress();
    }

    @Override
    public int getSessionId() {
        return getChannel().getChannelId();
    }

    @Override
    public boolean isClosed() {
        return !isOpened();
    }

    @Override
    public boolean isOpened() {
        return getChannel().isOpened();
    }

    @Override
    public Object removeAttribute(Object key) {
        return attributes.remove(key);
    }

    @Override
    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public void setAttribute(Object key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public String toString() {
        return getChannel().toString();
    }

    @Override
    public ByteBufAllocator getByteBufAllocator() {
        return getChannel().getByteBufAllocator();
    }

    @Override
    public boolean inSelectorLoop() {
        return getChannel().inSelectorLoop();
    }

    @Override
    public void close() {
        CloseUtil.close(getChannel());
    }

}
