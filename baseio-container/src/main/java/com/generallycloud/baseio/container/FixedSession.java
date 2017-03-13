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
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.baseio.common.BeanUtil;
import com.generallycloud.baseio.common.ClassUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.MD5Token;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.OnReadFuture;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.WaiterOnReadFuture;
import com.generallycloud.baseio.container.authority.Authority;
import com.generallycloud.baseio.protocol.ReadFuture;

public class FixedSession {

	private Authority			authority		= null;
	private SocketChannelContext	context		= null;
	private AtomicBoolean		logined		= new AtomicBoolean(false);
	private SocketSession		session		= null;
	private long				timeout		= 50000;
	private SimpleIoEventHandle	eventHandle	= null;

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

	public void accept(SocketSession session, ReadFuture future) throws Exception {

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
		return logined.get();
	}

	public boolean login(String username, String password) {

		RESMessage message = login4RES(username, password);

		return message.getCode() == 0;
	}

	public RESMessage login4RES(String username, String password) {

		if (!logined.compareAndSet(false, true)) {

			return RESMessage.SUCCESS;
		}

		try {

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("username", username);
			param.put("password", MD5Token.getInstance().getLongToken(password, Encoding.UTF8));

			String paramString = JSON.toJSONString(param);

			ProtobaseReadFuture future = request("login", paramString);

			RESMessage message = RESMessageDecoder.decode(future.getReadText());

			if (message.getCode() == 0) {

				JSONObject o = (JSONObject) message.getData();

				String className = o.getString("className");

				Class<?> clazz = ClassUtil.forName(className);

				Authority authority = (Authority) BeanUtil.map2Object(o, clazz);

				setAuthority(authority);

			} else {
				logined.compareAndSet(true, false);
			}

			return message;
		} catch (Exception e) {
			logined.compareAndSet(true, false);
			return new RESMessage(400, e.getMessage());
		}
	}

	public void logout() {
		// TODO complete logout
	}

	public ProtobaseReadFuture request(String serviceName, String content) throws IOException {
		return request(serviceName, content, null);
	}

	public ProtobaseReadFuture request(String serviceName, String content, byte[] binary) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		ProtobaseReadFuture readFuture = new ProtobaseReadFutureImpl(context, serviceName);

		readFuture.setIoEventHandle(eventHandle);

		readFuture.write(content);

		if (binary != null) {
			readFuture.writeBinary(binary);
		}

		WaiterOnReadFuture onReadFuture = new WaiterOnReadFuture();

		waiterListen(serviceName, onReadFuture);

		session.flush(readFuture);

		// FIXME 连接丢失时叫醒我
		if (onReadFuture.await(timeout)) {

			CloseUtil.close(session);

			throw new TimeoutException("timeout");
		}

		return (ProtobaseReadFuture) onReadFuture.getReadFuture();
	}

	public void setAuthority(Authority authority) {
		this.authority = authority;
	}

	public void update(SocketSession session) {
		this.session = session;
		this.context = session.getContext();
		this.eventHandle = (SimpleIoEventHandle) context.getIoEventHandleAdaptor();
	}

	private void waiterListen(String serviceName, WaiterOnReadFuture onReadFuture) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		if (onReadFuture == null) {
			throw new IOException("empty onReadFuture");
		}

		OnReadFutureWrapper wrapper = eventHandle.getOnReadFutureWrapper(serviceName);

		if (wrapper == null) {

			wrapper = new OnReadFutureWrapper();

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

		ProtobaseReadFuture readFuture = new ProtobaseReadFutureImpl(context, serviceName);

		readFuture.setIoEventHandle(eventHandle);

		readFuture.write(content);

		if (binary != null) {
			readFuture.writeBinary(binary);
		}

		session.flush(readFuture);
	}

	public void listen(String serviceName, OnReadFuture onReadFuture) throws IOException {
		eventHandle.listen(serviceName, onReadFuture);
	}

}
