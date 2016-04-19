package com.gifisan.nio.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.NetworkWeakException;
import com.gifisan.nio.TimeoutException;
import com.gifisan.nio.client.session.ClientSessionFactory;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DateUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.StreamUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ClientMultiDecoder;
import com.gifisan.nio.component.protocol.ClientStreamDecoder;
import com.gifisan.nio.component.protocol.TextDecoder;

public class ClientConnection implements Connectable, Closeable {

	private AtomicBoolean		connected		= new AtomicBoolean(false);
	private ClientProtocolEncoder	encoder		= new ClientProtocolEncoder();
	private ClientEndPoint		endPoint		= null;
	private String				host			= null;
	private boolean			netweak		= false;
	private int				port			= 0;
	private Selector			selector		= null;
	private InetSocketAddress	serverAddress	= null;
	private boolean			close		= false;
	private boolean			closed		= false;
	private boolean			unique		= true;
	private ProtocolDecoder		decoder		= null;
	private ClientConnector 		connector		= null;
	private byte[]			beat			= { 3 };

	public ClientConnection(String host, int port,ClientConnector connector) {
		this.host = host;
		this.port = port;
		this.connector = connector;
		this.decoder = new ClientProtocolDecoder(
				new TextDecoder(Encoding.DEFAULT),
				new ClientStreamDecoder(Encoding.DEFAULT),
				new ClientMultiDecoder(Encoding.DEFAULT));
	}

	private ClientResponse acceptResponse(ClientEndPoint endPoint) throws IOException {

		ClientResponse response = new ClientResponse();

		if (!decoder.decode(endPoint, response)) {

			if (endPoint.isEndConnect()) {
				CloseUtil.close(connector);
				
				throw new NetworkWeakException("newwork weak,please reconnect after a while");
			}
			
			throw new IOException("protocol type:" + response.getProtocolType());
		}
		
		return response;
	}

	public ClientResponse acceptResponse() throws IOException {
		
		for (;;) {
			
			endPoint.register(selector, SelectionKey.OP_READ);
			
			Iterator<SelectionKey> iterator = select(1000);
			
			if (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
				return acceptResponse(endPoint);
			}

			if (close) {
				this.closed = true;
				return null;
			}

			if (netweak) {
				throw new NetworkWeakException("server is unavailable");
			}

		}
	}

	public ClientResponse acceptResponse(long timeout) throws IOException {
		
		if (timeout == 0) {
			return acceptResponse();
		}
		
		Iterator<SelectionKey> iterator = select(timeout);

		if (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			return acceptResponse(endPoint);
		}

		throw new TimeoutException("server is unavailable");

	}

	private void checkConnector() throws IOException {
		if (!connected.get()) {
			throw new IOException("disconnected from server");
		}
	}

	public void close() throws IOException {
		if (connected.compareAndSet(true, false)) {
			if (unique) {
				CloseUtil.close(endPoint);
				this.selector.close();
			} else {
				this.close = true;
				this.wakeup();
				for (; !closed;) {
					ThreadUtil.sleep(1);
					this.wakeup();
				}
				CloseUtil.close(endPoint);
				this.selector.close();
			}
		}
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {
			this.serverAddress = new InetSocketAddress(host, port);
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(false);
			selector = Selector.open();
			channel.register(selector, SelectionKey.OP_CONNECT);
			channel.connect(serverAddress);
			connect0(selector);
		}
	}

	public void connect(boolean multi) throws IOException {
		this.unique = !multi;
		this.connect();
	}

	private void connect0(Selector selector) throws IOException {
		Iterator<SelectionKey> iterator = select(0);
		finishConnect(iterator);
	}
	
	private void finishConnect(Iterator<SelectionKey> iterator) throws IOException{
		for (; iterator.hasNext();) {
			SelectionKey selectionKey = iterator.next();
			iterator.remove();
			finishConnect0(selectionKey);
		}
	}
	
	private void finishConnect0(SelectionKey selectionKey) throws IOException{
		SocketChannel channel = (SocketChannel) selectionKey.channel();
		if (selectionKey.isConnectable() && channel.isConnectionPending()) {
			channel.finishConnect();
			ClientSessionFactory clientSessionFactory = connector.getClientSessionFactory();
			this.netweak = false;
			this.endPoint = new ClientEndPoint(selectionKey,clientSessionFactory);
		}
	}


	private Iterator<SelectionKey> select(long timeout) throws IOException{
		selector.select(timeout);
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		return selectionKeys.iterator();
	}
	
	protected void setNetworkWeak() {
		this.netweak = true;
	}

	protected void wakeup() {
		this.selector.wakeup();
	}

	protected void write(byte sessionID, String serviceName, String text) throws IOException {

		checkConnector();

		ClientEndPoint endPoint = this.endPoint;

//		Selector selector = this.selector;

		// endPoint.register(selector, SelectionKey.OP_WRITE);

		ByteBuffer buffer = encoder.encode(sessionID, serviceName, text, Encoding.DEFAULT);

		buffer.flip();

		endPoint.write(buffer);

//		endPoint.register(selector, SelectionKey.OP_READ);
	}

	protected void write(byte sessionID, String serviceName, String text, InputStream inputStream) throws IOException {

		checkConnector();

		ClientEndPoint endPoint = this.endPoint;

//		Selector selector = this.selector;

		// endPoint.register(selector, SelectionKey.OP_WRITE);

		ByteBuffer buffer = encoder.encode(sessionID, serviceName, text, inputStream.available(), Encoding.DEFAULT);

		buffer.flip();

		endPoint.write(buffer);
		
		StreamUtil.write(inputStream, endPoint, 0, inputStream.available(), 102400);

//		endPoint.register(selector, SelectionKey.OP_READ);
	}

	protected void writeBeat() throws IOException {
		checkConnector();
		
		endPoint.write(beat);
		
		DebugUtil.debug(">>write beat........." + DateUtil.now());
	}

	public String toString() {
		return host+":"+port;
	}
	
}
