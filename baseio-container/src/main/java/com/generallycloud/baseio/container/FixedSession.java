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
package com.generallycloud.baseio.container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFutureImpl;
import com.generallycloud.baseio.common.BeanUtil;
import com.generallycloud.baseio.common.ClassUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.MD5Util;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.authority.Authority;
import com.generallycloud.baseio.protocol.Future;

public class FixedSession {

    private Authority            authority   = null;
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

    public void accept(SocketSession session, Future future) throws Exception {

    }

    public Authority getAuthority() {
        return authority;
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

    public boolean login(String username, String password) {

        RESMessage message = login4RES(username, password);

        return message.getCode() == 0;
    }

    public RESMessage login4RES(String username, String password) {

        try {

            Map<String, Object> param = new HashMap<>();
            param.put("username", username);
            param.put("password",MD5Util.get().get32(password, context.getEncoding()));

            String paramString = JSON.toJSONString(param);

            ProtobaseFuture future = request(ContainerConsotant.ACTION_LOGIN, paramString);

            RESMessage message = RESMessageDecoder.decode(future.getReadText());

            if (message.getCode() == 0) {

                JSONObject o = (JSONObject) message.getData();

                String className = o.getString("className");

                Class<?> clazz = ClassUtil.forName(className);

                Authority authority = (Authority) BeanUtil.map2Object(o, clazz);

                setAuthority(authority);

            }

            return message;
        } catch (Exception e) {
            return new RESMessage(400, e.getMessage());
        }
    }

    public void logout() {
        // TODO complete logout
    }

    public ProtobaseFuture request(String serviceName, String content) throws IOException {
        return request(serviceName, content, null);
    }

    public ProtobaseFuture request(String serviceName, String content, byte[] binary)
            throws IOException {

        if (StringUtil.isNullOrBlank(serviceName)) {
            throw new IOException("empty service name");
        }

        ProtobaseFuture future = new ProtobaseFutureImpl(context, serviceName);

        future.write(content);

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

        return (ProtobaseFuture) onReadFuture.getReadFuture();
    }

    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    public void update(SocketSession session) {
        this.session = session;
        this.context = session.getContext();
        this.eventHandle = (SimpleIoEventHandle) context.getIoEventHandleAdaptor();
    }

    private void waiterListen(String serviceName, WaiterOnFuture onReadFuture) throws IOException {

        if (StringUtil.isNullOrBlank(serviceName)) {
            throw new IOException("empty service name");
        }

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
        if (StringUtil.isNullOrBlank(serviceName)) {
            throw new IOException("empty service name");
        }

        ProtobaseFuture future = new ProtobaseFutureImpl(context, serviceName);

        future.write(content);

        if (binary != null) {
            future.writeBinary(binary);
        }

        session.flush(future);
    }

    public void listen(String serviceName, OnFuture onReadFuture) throws IOException {
        eventHandle.listen(serviceName, onReadFuture);
    }

}
