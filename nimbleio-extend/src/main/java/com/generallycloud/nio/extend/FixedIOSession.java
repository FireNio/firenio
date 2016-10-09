package com.generallycloud.nio.extend;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.codec.nio.future.NIOReadFutureImpl;
import com.generallycloud.nio.common.BeanUtil;
import com.generallycloud.nio.common.ClassUtil;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.MD5Token;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.WaiterOnReadFuture;
import com.generallycloud.nio.extend.plugin.authority.SYSTEMAuthorityServlet;
import com.generallycloud.nio.extend.security.Authority;
import com.generallycloud.nio.protocol.NamedReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public class FixedIOSession implements FixedSession {

	private Authority						authority;
	private NIOContext						context;
	private Map<String, OnReadFutureWrapper>	listeners	= new HashMap<String, OnReadFutureWrapper>();
	private AtomicBoolean					logined	= new AtomicBoolean(false);
	private Session						session;
	// FIXME timeout
	private long							timeout	= 50000;

	public void setTimeout(long timeout) {
		if (timeout < 0) {
			throw new IllegalArgumentException("illegal argument timeout: " + timeout);
		}

		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}

	public void accept(Session session, ReadFuture future) throws Exception {

		NamedReadFuture f = (NamedReadFuture) future;

		OnReadFutureWrapper onReadFuture = listeners.get(f.getFutureName());

		if (onReadFuture != null) {
			onReadFuture.onResponse(this.getSession(), f);
		}
	}

	public Authority getAuthority() {
		return authority;
	}

	public NIOContext getContext() {
		return context;
	}

	public Session getSession() {
		return session;
	}

	public boolean isLogined() {
		return logined.get();
	}

	public void listen(String serviceName, OnReadFuture onReadFuture) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		OnReadFutureWrapper wrapper = listeners.get(serviceName);

		if (wrapper == null) {

			wrapper = new OnReadFutureWrapper();

			listeners.put(serviceName, wrapper);
		}

		if (onReadFuture == null) {
			return;
		}

		wrapper.setListener(onReadFuture);
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
			param.put("password", MD5Token.getInstance().getLongToken(password, Encoding.DEFAULT));

			String paramString = JSONObject.toJSONString(param);

			NIOReadFuture future = request(SYSTEMAuthorityServlet.SERVICE_NAME, paramString);

			RESMessage message = RESMessageDecoder.decode(future.getText());

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

	public NIOReadFuture request(String serviceName, String content) throws IOException {
		return request(serviceName, content, null);
	}

	public NIOReadFuture request(String serviceName, String content, byte[] binary) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		NIOReadFuture readFuture = new NIOReadFutureImpl(serviceName);
		
		readFuture.setIOEventHandle(context.getIOEventHandleAdaptor());
		
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

		return (NIOReadFuture) onReadFuture.getReadFuture();
	}

	@SuppressWarnings("rawtypes")
	public NIOReadFuture request(String serviceName, Map params, InputStream inputStream) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		BinaryFlusher flusher = new BinaryFlusher(inputStream, session, serviceName, params);

		WaiterOnReadFuture onReadFuture = new WaiterOnReadFuture();

		waiterListen(serviceName, onReadFuture);

		flusher.flush();

		// FIXME 连接丢失时叫醒我
		if (!onReadFuture.await(timeout)) {

			return (NIOReadFuture) onReadFuture.getReadFuture();
		}

		CloseUtil.close(session);

		throw new TimeoutException("timeout");
	}

	public void setAuthority(Authority authority) {
		this.authority = authority;
	}

	public void update(Session session) {
		this.session = session;
		this.context = session.getContext();
	}

	private void waiterListen(String serviceName, WaiterOnReadFuture onReadFuture) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		if (onReadFuture == null) {
			throw new IOException("empty onReadFuture");
		}

		OnReadFutureWrapper wrapper = listeners.get(serviceName);

		if (wrapper == null) {

			wrapper = new OnReadFutureWrapper();

			listeners.put(serviceName, wrapper);
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

		NIOReadFuture readFuture = new NIOReadFutureImpl(serviceName);
		
		readFuture.setIOEventHandle(context.getIOEventHandleAdaptor());
		
		readFuture.write(content);

		if (binary != null) {
			readFuture.writeBinary(binary);
		}

		session.flush(readFuture);
	}

}
