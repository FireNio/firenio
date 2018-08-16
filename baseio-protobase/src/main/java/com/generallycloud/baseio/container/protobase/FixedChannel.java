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
import java.util.Map;

import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.codec.protobase.ParamedProtobaseFrame;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;

public class FixedChannel {

    private ChannelContext      context     = null;
    private boolean             logined     = false;
    private NioSocketChannel    ch     = null;
    private long                timeout     = 50000;
    private SimpleIoEventHandle eventHandle = null;

    public FixedChannel(NioSocketChannel ch) {
        update(ch);
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
        return ch;
    }

    public boolean isLogined() {
        return logined;
    }

    public ParamedProtobaseFrame request(String serviceName) throws IOException {
        return request(serviceName, null, null);
    }

    public ParamedProtobaseFrame request(String serviceName, Map<String, Object> params)
            throws IOException {
        return request(serviceName, params, null);
    }

    public ParamedProtobaseFrame request(String serviceName, Map<String, Object> params,
            byte[] binary) throws IOException {
        ParamedProtobaseFrame frame = new ParamedProtobaseFrame(serviceName);
        if (params != null) {
            frame.putAll(params);
        }
        if (binary != null) {
            frame.writeBinary(binary);
        }
        WaiterOnFrame onReadFrame = new WaiterOnFrame();
        waiterListen(serviceName, onReadFrame);
        ch.flush(frame);
        // FIXME 连接丢失时叫醒我
        if (onReadFrame.await(timeout)) {
            CloseUtil.close(ch);
            throw new TimeoutException("timeout");
        }
        return (ParamedProtobaseFrame) onReadFrame.getReadFrame();
    }

    public void update(NioSocketChannel ch) {
        this.ch = ch;
        this.context = ch.getContext();
        this.eventHandle = (SimpleIoEventHandle) ch.getIoEventHandle();
    }

    private void waiterListen(String serviceName, WaiterOnFrame onReadFrame) throws IOException {
        if (onReadFrame == null) {
            throw new IOException("empty onReadFrame");
        }
        OnFrameWrapper wrapper = eventHandle.getOnReadFrameWrapper(serviceName);
        if (wrapper == null) {
            wrapper = new OnFrameWrapper();
            eventHandle.putOnReadFrameWrapper(serviceName, wrapper);
        }
        wrapper.listen(onReadFrame);
    }

    public void write(String serviceName) throws IOException {
        write(serviceName, null, null);
    }

    public void write(String serviceName, Map<String, Object> params) throws IOException {
        write(serviceName, params, null);
    }

    public void write(String serviceName, Map<String, Object> params, byte[] binary)
            throws IOException {
        ParamedProtobaseFrame frame = new ParamedProtobaseFrame(serviceName);
        if (params != null) {
            frame.putAll(params);
        }
        if (binary != null) {
            frame.writeBinary(binary);
        }
        ch.flush(frame);
    }

    public void listen(String serviceName, OnFrame onReadFrame) throws IOException {
        eventHandle.listen(serviceName, onReadFrame);
    }

}
