package com.gifisan.mtp.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.common.DateUtil;
import com.gifisan.mtp.common.DebugUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.Connectable;
import com.gifisan.mtp.component.EndPoint;
import com.gifisan.mtp.component.InputStream;

public class ClientConnection implements Connectable, Closeable {

	private AtomicBoolean		connected		= new AtomicBoolean(false);
	private ProtocolEncoder		encoder		= new ProtocolEncoder();
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

	public ClientConnection(String host, int port) {
		this.host = host;
		this.port = port;
	}

	private Response acceptResponse(ClientEndPoint endPoint) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(6);
		int _length = endPoint.read(buffer);
		if (_length < 6) {
			return null;
		}

		byte[] header = buffer.array();
		byte type = header[0];
		byte sessionID = header[1];
		int length = getLength(header);

		if (type == Response.TEXT) {
			ByteBuffer _buffer = read(endPoint, length);
			String content = new String(_buffer.array(), Encoding.DEFAULT);
			return new Response(content, sessionID);
		} else if (type == Response.STREAM) {
			InputStream inputStream = new ClientInputStream(endPoint, length);
			return new Response(inputStream);
		} else {
			throw new IOException("unknow header: " + type);
		}
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

	protected Response acceptResponse(long timeout) throws IOException {
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
			}else{
				this.close = true;
				this.wakeup();
				for (; !closed;) {
					try {
						Thread.sleep(1);
						this.wakeup();
					} catch (InterruptedException e) {
						DebugUtil.debug(e);
					}
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
					this.endPoint = new ClientEndPointImpl(channel);
				}
			}
		}
	}

	private int getLength(byte[] header) {
		int v0 = (header[2] & 0xff);
		int v1 = (header[3] & 0xff) << 8;
		int v2 = (header[4] & 0xff) << 16;
		int v3 = (header[5] & 0xff) << 24;
		return v0 | v1 | v2 | v3;
	}

	protected void keepAlive() {
		this.writer = new AliveClientWriter();
	}

	private ByteBuffer read(EndPoint endPoint, int length) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(length);
		int _length = endPoint.read(buffer);
		for (; _length < length;) {
			int __length = endPoint.read(buffer);
			_length += __length;
		}
		return buffer;
	}

	protected void setNetworkWeak() {
		this.netweak = true;
	}

	protected void wakeup() {
		this.selector.wakeup();
	}

	protected void write(byte sessionID, String serviceName, String content) throws IOException {
		checkConnector();
		Selector selector = this.selector;
		// endPoint.register(selector, SelectionKey.OP_WRITE);

		Charset charset = Encoding.DEFAULT;

		ByteBuffer buffer = null;

		if (StringUtil.isNullOrBlank(content)) {
			buffer = encoder.encode(sessionID, serviceName.getBytes(charset));
		} else {
			buffer = encoder.encode(sessionID, serviceName.getBytes(charset), content.getBytes(charset));
		}
		buffer.flip();
		ClientEndPoint endPoint = this.endPoint;
		writer.writeText(endPoint, buffer);
		endPoint.register(selector, SelectionKey.OP_READ);
	}

	protected void write(byte sessionID, String serviceName, String content, java.io.InputStream inputStream)
			throws IOException {

		checkConnector();
		ClientEndPoint endPoint = this.endPoint;
		Selector selector = this.selector;
		// endPoint.register(selector, SelectionKey.OP_WRITE);
		int avaiable = inputStream.available();
		Charset charset = Encoding.DEFAULT;

		ByteBuffer buffer = null;

		if (StringUtil.isNullOrBlank(content)) {
			buffer = encoder.encode(sessionID, serviceName.getBytes(charset), avaiable);
		} else {
			buffer = encoder.encode(sessionID, serviceName.getBytes(charset), content.getBytes(charset), avaiable);
		}

		buffer.flip();

		writer.writeStream(endPoint, inputStream, buffer, 102400);

		endPoint.register(selector, SelectionKey.OP_READ);

	}

	protected void writeBeat() throws IOException {
		checkConnector();
		writer.writeBeat(endPoint);
		System.out.println(">>write beat........." + DateUtil.now());
	}

}
