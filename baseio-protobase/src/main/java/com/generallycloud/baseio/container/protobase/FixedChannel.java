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
import com.generallycloud.baseio.codec.protobase.ParamedProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.ParamedProtobaseFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;

public class FixedChannel {

    private ChannelContext      context     = null;
    private boolean             logined     = false;
    private NioSocketChannel    channel     = null;
    private long                timeout     = 50000;
    private SimpleIoEventHandle eventHandle = null;

    public FixedChannel(NioSocketChannel channel) {
        update(channel);
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

    public ChannelContext getContext() {
        return context;
    }

    public NioSocketChannel getChannel() {
        return channel;
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
            future.write(content, channel.getEncoding());
        }
        if (binary != null) {
            future.writeBinary(binary);
        }
        WaiterOnFuture onReadFuture = new WaiterOnFuture();
        waiterListen(serviceName, onReadFuture);
        channel.flush(future);
        // FIXME 连接丢失时叫醒我
        if (onReadFuture.await(timeout)) {
            CloseUtil.close(channel);
            throw new TimeoutException("timeout");
        }
        return (ParamedProtobaseFuture) onReadFuture.getReadFuture();
    }

    public void update(NioSocketChannel channel) {
        this.channel = channel;
        this.context = channel.getContext();
        this.eventHandle = (SimpleIoEventHandle) channel.getIoEventHandle();
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
            future.write(content, channel.getEncoding());
        }
        if (binary != null) {
            future.writeBinary(binary);
        }
        channel.flush(future);
    }

    public void listen(String serviceName, OnFuture onReadFuture) throws IOException {
        eventHandle.listen(serviceName, onReadFuture);
    }

}
