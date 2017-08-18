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

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Map;

import com.generallycloud.baseio.buffer.ByteBufAllocator;

public interface Session extends Closeable {

    public abstract void active();

    public abstract void clearAttributes();

    public abstract boolean isClosed();

    public abstract Object getAttachment();

    public abstract Object getAttribute(Object key);

    public abstract Map<Object, Object> getAttributes();

    public abstract ChannelContext getContext();

    public abstract long getCreationTime();

    public abstract long getLastAccessTime();

    public abstract String getLocalAddr();

    public abstract String getLocalHost();

    public abstract int getLocalPort();

    public abstract InetSocketAddress getLocalSocketAddress();

    public abstract String getRemoteAddr();

    public abstract String getRemoteHost();

    public abstract int getRemotePort();

    public abstract InetSocketAddress getRemoteSocketAddress();

    public abstract int getSessionId();

    public abstract Object removeAttribute(Object key);

    public abstract void setAttachment(Object attachment);

    public abstract void setAttribute(Object key, Object value);

    public abstract Charset getEncoding();

    public abstract boolean isOpened();

    public abstract ByteBufAllocator getByteBufAllocator();

    public abstract boolean inSelectorLoop();

}
