package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.security.Authority;

public abstract class AbstractClientSession extends AbstractSession implements ProtectedClientSession {

	protected ClientContext					context			= null;
	protected DatagramPacketAcceptor			dpAcceptor		= null;
	private ThreadPool						executor			= null;
	private Map<String, OnReadFuture>			listeners			= new HashMap<String, OnReadFuture>();
	private Map<String, ClientStreamAcceptor>	streamAcceptors	= new HashMap<String, ClientStreamAcceptor>();
	private Authority						authority			= null;

	public AbstractClientSession(TCPEndPoint endPoint) {
		super(endPoint);
		this.context = (ClientContext) endPoint.getContext();
		this.executor = context.getExecutorThreadPool();
	}

	/**
	 * 不建议使用
	 */
	@Deprecated
	public void cancelListen(String serviceName) {
		this.listeners.remove(serviceName);
	}

	public void destroyImmediately() {

		super.destroyImmediately();
	}

	public ClientContext getContext() {
		return context;
	}

	public DatagramPacketAcceptor getDatagramPacketAcceptor() {
		return dpAcceptor;
	}

	public String getSessionID() {
		// if (sessionID == null) {
		//
		// try {
		//
		// WaiterOnReadFuture waiterOnReadFuture = new WaiterOnReadFuture();
		//
		// listen(SYSTEMAuthorityServlet.SERVICE_NAME, waiterOnReadFuture);
		//
		// write(SYSTEMAuthorityServlet.SERVICE_NAME, null);
		//
		// if (waiterOnReadFuture.await(3000)) {
		//
		// ReadFuture future = waiterOnReadFuture.getReadFuture();
		//
		// if (future instanceof ErrorReadFuture) {
		//
		// ErrorReadFuture _Future = ((ErrorReadFuture) future);
		//
		// throw new IOException(_Future.getException());
		// }
		//
		// sessionID = future.getText();
		// }
		//
		// } catch (IOException e) {
		// logger.debug(e);
		// }
		//
		// }
		return sessionID;
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

		if (closed()) {
			throw DisconnectException.INSTANCE;
		}

		this.listeners.put(serviceName, onReadFuture);

	}

	public void offerReadFuture(final IOReadFuture future) {
		final OnReadFuture onReadFuture = listeners.get(future.getServiceName());

		if (onReadFuture != null) {

			this.executor.dispatch(new Runnable() {

				public void run() {
					onReadFuture.onResponse((ProtectedClientSession) future.getSession(), future);
				}
			});
		}
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

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public Authority getAuthority() {
		return authority;
	}

	public void setAuthority(Authority authority) {
		this.authority = authority;
	}
	
	

}
