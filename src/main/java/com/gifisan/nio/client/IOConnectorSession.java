package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.TimeoutException;
import com.gifisan.nio.common.BeanUtil;
import com.gifisan.nio.common.ClassUtil;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.common.Waiter;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.plugin.authority.SYSTEMAuthorityServlet;
import com.gifisan.nio.plugin.rtp.server.RTPServerDPAcceptor;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.RESMessageDecoder;
import com.gifisan.security.Authority;

public class IOConnectorSession implements ConnectorSession {

	protected NIOContext					context			= null;
	protected DatagramPacketAcceptor			dpAcceptor		= null;
	private ThreadPool						executor			= null;
	private Map<String, OnReadFuture>			listeners			= new HashMap<String, OnReadFuture>();
	private Map<String, ClientStreamAcceptor>	streamAcceptors	= new HashMap<String, ClientStreamAcceptor>();
	private Authority						authority			= null;
	private Session						session			= null;
	private UDPConnector					udpConnector		= null;

	public IOConnectorSession(Session session) {
		this.session = session;
		this.executor = context.getThreadPool();
	}

	public DatagramPacketAcceptor getDatagramPacketAcceptor() {
		return dpAcceptor;
	}

	public ReadFuture request(String serviceName, String content, InputStream inputStream, long timeout)
			throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		ReadFuture readFuture = ReadFutureFactory.create(session, serviceName);

		readFuture.write(content);

		readFuture.setInputStream(inputStream);

		session.flush(readFuture);

		WaiterOnReadFuture onReadFuture = new WaiterOnReadFuture();

		listen(serviceName, onReadFuture);

		if (onReadFuture.await(timeout)) {

			return onReadFuture.getReadFuture();
		}

		throw new TimeoutException("timeout");
	}

	public void write(String serviceName, String content, InputStream inputStream) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		ReadFuture readFuture = ReadFutureFactory.create(session, serviceName);

		readFuture.write(content);

		readFuture.setInputStream(inputStream);

		session.flush(readFuture);
	}

	public ClientStreamAcceptor getStreamAcceptor(String serviceName) {
		return streamAcceptors.get(serviceName);
	}

	public void listen(String serviceName, OnReadFuture onReadFuture) throws IOException {
		
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		if (onReadFuture == null) {
			onReadFuture = OnReadFuture.EMPTY_ON_READ_FUTURE;
		}

		this.listeners.put(serviceName, onReadFuture);

	}

	public void offerReadFuture(final IOReadFuture future) {
		final OnReadFuture onReadFuture = listeners.get(future.getServiceName());

		if (onReadFuture != null) {

			this.executor.dispatch(new Runnable() {

				public void run() {
					onReadFuture.onResponse(IOConnectorSession.this, future);
				}
			});
		}
	}
	
	public Session getSession() {
		return session;
	}

	public void onStreamRead(String key, ClientStreamAcceptor acceptor) {
		streamAcceptors.put(key, acceptor);
	}

	public ReadFuture request(String serviceName, String content) throws IOException {
		return request(serviceName, content, 3000000);
	}

	public ReadFuture request(String serviceName, String content, InputStream inputStream) throws IOException {
		return request(serviceName, content, inputStream, 3000000);
	}

	public ReadFuture request(String serviceName, String content, long timeout) throws IOException {
		return request(serviceName, content, null, timeout);
	}

	public void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor) {
		this.dpAcceptor = datagramPacketAcceptor;
	}

	public void write(String serviceName, String content) throws IOException {
		write(serviceName, content, null);
	}

	public Authority getAuthority() {
		return authority;
	}

	public void setAuthority(Authority authority) {
		this.authority = authority;
	}

	private AtomicBoolean	logined	= new AtomicBoolean(false);
	

	public UDPConnector getUdpConnector() {
		return udpConnector;
	}

	public void setUdpConnector(UDPConnector udpConnector) {
		this.udpConnector = udpConnector;
	}

	public RESMessage login4RES(String username, String password) {

		if (logined.compareAndSet(false, true)) {

			try {

				Map<String, Object> param = new HashMap<String, Object>();
				param.put("username", username);
				param.put("password", MD5Token.getInstance().getLongToken(password, Encoding.DEFAULT));
				param.put("MACHINE_TYPE", session.getMachineType());

				String paramString = JSONObject.toJSONString(param);

				ReadFuture future = request(SYSTEMAuthorityServlet.SERVICE_NAME, paramString);

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

		return RESMessage.SUCCESS;
	}

	public boolean login(String username, String password) {

		RESMessage message = login4RES(username, password);

		return message.getCode() == 0;
	}

	public void logout() {
		// TODO complete logout
	}

	public boolean isLogined() {
		return logined.get();
	}

	public void bindUDPSession() throws IOException {
		
		if (udpConnector == null) {
			throw new IllegalArgumentException("null udp connector");
		}

		Session session = this.session;

		Long sessionID = session.getSessionID();

		JSONObject json = new JSONObject();

		//FIXME add more info
		json.put("serviceName", RTPServerDPAcceptor.BIND_SESSION);

		json.put("sessionID", sessionID);

		final DatagramPacket packet = new DatagramPacket(json.toJSONString().getBytes(context.getEncoding()));

		final String BIND_SESSION_CALLBACK = RTPServerDPAcceptor.BIND_SESSION_CALLBACK;

		final CountDownLatch latch = new CountDownLatch(1);
		
		listen(BIND_SESSION_CALLBACK, new OnReadFuture() {

			public void onResponse(ConnectorSession session, ReadFuture future) {

				latch.countDown();

			}
		});

		final Waiter<Integer> waiter = new Waiter<Integer>();

		ThreadUtil.execute(new Runnable() {

			public void run() {
				for (int i = 0; i < 10; i++) {

					udpConnector.sendDatagramPacket(packet);

					try {
						if (latch.await(300, TimeUnit.MILLISECONDS)) {

							waiter.setPayload(0);

							break;
						}
					} catch (InterruptedException e) {

						CloseUtil.close(udpConnector);
					}
				}
			}
		});

		if (!waiter.await(3000)) {
			CloseUtil.close(udpConnector);

			throw DisconnectException.INSTANCE;
		}
	}
}
