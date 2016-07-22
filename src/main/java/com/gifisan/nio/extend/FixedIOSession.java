package com.gifisan.nio.extend;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.TimeoutException;
import com.gifisan.nio.common.BeanUtil;
import com.gifisan.nio.common.ClassUtil;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.plugin.authority.SYSTEMAuthorityServlet;
import com.gifisan.nio.extend.security.Authority;

public class FixedIOSession implements FixedSession {

	private Authority						authority;
	private NIOContext						context;
	private Map<String, OnReadFutureWrapper>	listeners	= new HashMap<String, OnReadFutureWrapper>();
	private AtomicBoolean					logined	= new AtomicBoolean(false);
	private Session						session;

	public void accept(Session session, final NIOReadFuture future) throws Exception {

		OnReadFutureWrapper onReadFuture = listeners.get(future.getServiceName());

		if (onReadFuture != null) {
			onReadFuture.onResponse(this.getSession(), future);
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

		if (onReadFuture == null) {
			onReadFuture = OnReadFuture.EMPTY_ON_READ_FUTURE;
		}

		OnReadFutureWrapper wrapper = listeners.get(serviceName);

		if (wrapper == null) {

			wrapper = new OnReadFutureWrapper();

			listeners.put(serviceName, wrapper);
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
			param.put("MACHINE_TYPE", session.getMachineType());

			String paramString = JSONObject.toJSONString(param);

			NIOReadFuture future = request(SYSTEMAuthorityServlet.SERVICE_NAME, paramString);

			RESMessage message = RESMessageDecoder.decode(future.getText());

			if (message.getCode() == 0) {

				JSONObject o = (JSONObject) message.getData();

				String className = o.getString("className");

				Class clazz = ClassUtil.forName(className);

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
		return request(serviceName, content, 3000);
	}

	public NIOReadFuture request(String serviceName, String content, InputStream inputStream) throws IOException {
		return request(serviceName, content, inputStream, 3000);
	}

	public NIOReadFuture request(String serviceName, String content, InputStream inputStream, long timeout)
			throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		NIOReadFuture readFuture = ReadFutureFactory.create(session, serviceName,context.getIOEventHandleAdaptor());

		readFuture.write(content);

		readFuture.setInputStream(inputStream);

		WaiterOnReadFuture onReadFuture = new WaiterOnReadFuture();

		waiterListen(serviceName, onReadFuture);

		session.flush(readFuture);

		// FIXME 连接丢失时叫醒我
		if (onReadFuture.await(timeout)) {

			return (NIOReadFuture) onReadFuture.getReadFuture();
		}

		throw new TimeoutException("timeout");
	}

	public NIOReadFuture request(String serviceName, String content, long timeout) throws IOException {
		return request(serviceName, content, null, timeout);
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

	public void write(String serviceName, String content, InputStream inputStream) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		NIOReadFuture readFuture = ReadFutureFactory.create(session, serviceName,context.getIOEventHandleAdaptor());

		readFuture.write(content);

		readFuture.setInputStream(inputStream);
		
		session.flush(readFuture);
	}
}
