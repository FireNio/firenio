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
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DateUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.Connectable;
import com.gifisan.nio.component.ProtocolData;
import com.gifisan.nio.component.ProtocolDecoder;

public class ClientConnection implements Connectable, Closeable {

	private AtomicBoolean		connected		= new AtomicBoolean(false);
	private ClientProtocolEncoder	encoder		= new ClientProtocolEncoder();
	private ClientEndPoint		endPoint		= null;
	private String				host			= null;
	private boolean			netweak		= false;
	private int				port			= 0;
	private Selector			selector		= null;
	private InetSocketAddress	serverAddress	= null;
	private ClientWriter		writer		= new NormalClientWriter();
	private boolean			close		= false;
	private boolean			closed		= false;
	private boolean			unique		= true;
	private ProtocolDecoder		decoder		= new ClientProtocolDecoder();
	private ClientConnector 		connector		= null;

	public ClientConnection(String host, int port,ClientConnector connector) {
		this.host = host;
		this.port = port;
		this.connector = connector;
	}

	private Response acceptResponse(ClientEndPoint endPoint) throws IOException {

		Response response = new Response();

		if (!decoder.decode(endPoint, response, Encoding.DEFAULT)) {

			throw new IOException("protocol type:" + response.getProtocolType());
		}
		return response;
	}

	protected Response acceptResponse() throws IOException {
		for (;;) {
			selector.select(1000);
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
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

	protected ProtocolData acceptResponse(long timeout) throws IOException {
		for (;;) {
			selector.select(1000);
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
			if (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
				return acceptResponse(endPoint);
			}

			if (netweak) {
				throw new NetworkWeakException("server is unavailable");
			}

		}
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

	public void connect(boolean unique) throws IOException {
		this.unique = unique;
		this.connect();
	}

	private void connect0(Selector selector) throws IOException {
		selector.select();
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectionKeys.iterator();
		for (; iterator.hasNext();) {
			SelectionKey selectionKey = iterator.next();
			iterator.remove();
			SocketChannel channel = (SocketChannel) selectionKey.channel();
			if (selectionKey.isConnectable()) {
				if (channel.isConnectionPending()) {
					channel.finishConnect();
					this.netweak = false;
					this.endPoint = new ClientEndPoint(selectionKey,connector);
				}
			}
		}
	}

	protected void keepAlive() {
		this.writer = new AliveClientWriter();
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

		Selector selector = this.selector;

		// endPoint.register(selector, SelectionKey.OP_WRITE);

		ByteBuffer buffer = encoder.encode(sessionID, serviceName, text, Encoding.DEFAULT);

		buffer.flip();

		writer.writeText(endPoint, buffer);

		endPoint.register(selector, SelectionKey.OP_READ);
	}

	protected void write(byte sessionID, String serviceName, String text, InputStream inputStream) throws IOException {

		checkConnector();

		ClientEndPoint endPoint = this.endPoint;

		Selector selector = this.selector;

		// endPoint.register(selector, SelectionKey.OP_WRITE);

		ByteBuffer buffer = encoder.encode(sessionID, serviceName, text, inputStream.available(), Encoding.DEFAULT);

		buffer.flip();

		writer.writeText(endPoint, buffer);
		
		writer.writeStream(endPoint, inputStream, 102400);

		endPoint.register(selector, SelectionKey.OP_READ);
	}

	protected void writeBeat() throws IOException {
		checkConnector();
		writer.writeBeat(endPoint);
		System.out.println(">>write beat........." + DateUtil.now());
	}

}
