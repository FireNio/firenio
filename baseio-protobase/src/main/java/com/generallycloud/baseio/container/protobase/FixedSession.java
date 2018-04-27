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
package com.generallycloud.baseio.container.protobase;

import java.io.IOException;

import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.codec.protobase.future.ParamedProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.future.ParamedProtobaseFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;

public class FixedSession {

    private SocketChannelContext context     = null;
    private boolean              logined     = false;
    private SocketSession        session     = null;
    private long                 timeout     = 50000;
    private SimpleIoEventHandle  eventHandle = null;

    public FixedSession(SocketSession session) {
        update(session);
    }

    public void setTimeout(long timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("illegal argument timeout: " + timeout);
        }

        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public SocketChannelContext getContext() {
        return context;
    }

    public SocketSession getSession() {
        return session;
    }

    public boolean isLogined() {
        return logined;
    }

    public ParamedProtobaseFuture request(String serviceName, String content) throws IOException {
        return request(serviceName, content, null);
    }

    public ParamedProtobaseFuture request(String serviceName, String content, byte[] binary)
            throws IOException {
        ParamedProtobaseFuture future = new ParamedProtobaseFutureImpl(serviceName);
        if (!StringUtil.isNullOrBlank(content)) {
            future.write(content,session.getEncoding());
        }
        if (binary != null) {
            future.writeBinary(binary);
        }
        WaiterOnFuture onReadFuture = new WaiterOnFuture();
        waiterListen(serviceName, onReadFuture);
        session.flush(future);
        // FIXME 连接丢失时叫醒我
        if (onReadFuture.await(timeout)) {
            CloseUtil.close(session);
            throw new TimeoutException("timeout");
        }
        return (ParamedProtobaseFuture) onReadFuture.getReadFuture();
    }

    public void update(SocketSession session) {
        this.session = session;
        this.context = session.getContext();
        this.eventHandle = (SimpleIoEventHandle) context.getIoEventHandleAdaptor();
    }

    private void waiterListen(String serviceName, WaiterOnFuture onReadFuture) throws IOException {
        if (onReadFuture == null) {
            throw new IOException("empty onReadFuture");
        }
        OnFutureWrapper wrapper = eventHandle.getOnReadFutureWrapper(serviceName);
        if (wrapper == null) {
            wrapper = new OnFutureWrapper();
            eventHandle.putOnReadFutureWrapper(serviceName, wrapper);
        }
        wrapper.listen(onReadFuture);
    }

    public void write(String serviceName, String content) throws IOException {
        write(serviceName, content, null);
    }

    public void write(String serviceName, String content, byte[] binary) throws IOException {
        ParamedProtobaseFuture future = new ParamedProtobaseFutureImpl(serviceName);
        if (!StringUtil.isNullOrBlank(content)) {
            future.write(content,session.getEncoding());
        }
        if (binary != null) {
            future.writeBinary(binary);
        }
        session.flush(future);
    }

    public void listen(String serviceName, OnFuture onReadFuture) throws IOException {
        eventHandle.listen(serviceName, onReadFuture);
    }

}
