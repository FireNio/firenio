package com.gifisan.mtp.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
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
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.EndPoint;

public class ClientConnection implements Closeable {

	private int				BLOCK		= 102400;
	private AtomicBoolean		connected		= new AtomicBoolean(false);
	private String				host			= null;
	private boolean			netweak		= false;
	private int				port			= 0;
	private Selector			selector		= null;
	private InetSocketAddress	serverAddress	= null;
	private ClientEndPoint		endPoint		= null;
	private ClientWriter		writer		= writers[0];

	public ClientConnection(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public Response acceptResponse(long timeout) throws IOException {
		if (timeout == 0) {
			for (;;) {
				selector.select(1000);
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectionKeys.iterator();
				if (iterator.hasNext()) {
					iterator.next();
					iterator.remove();
					return acceptResponse(endPoint, timeout);
				}
				if (netweak) {
					throw new NetworkWeakException("server is unavailable");
				}

			}
		} else {
			selector.select(timeout);
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
			if (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
				return acceptResponse(endPoint, timeout);
			}
			throw new IOException("read time out");
		}
	}

	private Response acceptResponse(ClientEndPoint endPoint, long timeout) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(5);
		int _length = endPoint.read(buffer);
		if (_length < 5) {
			return null;
		}
		byte[] header = buffer.array();

		byte type = header[0];
		int length = getLength(header);

		if (type == Response.TEXT) {
			String content = acceptString(endPoint, length, timeout);
			return new Response(content, type);
		} else if (type == Response.STREAM) {
			ClientInputStream inputStream = new ClientInputStream(endPoint, length);
			return new Response(inputStream);
		} else {
			throw new IOException("unknow header: " + type);
		}
	}

	private String acceptString(EndPoint endPoint, int length, long timeout) throws IOException {
		ByteBuffer buffer = read(endPoint, length, timeout);
		return new String(buffer.array(), Encoding.DEFAULT);
	}

	private void checkConnector() throws IOException {
		if (!connected.get()) {
			throw new IOException("disconnected from server");
		}

	}

	public void close() throws IOException {
		if (connected.compareAndSet(true, false)) {
			CloseUtil.close(endPoint);
			this.selector.close();
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

	private void connect0(Selector selector) throws IOException {
		selector.select();
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectionKeys.iterator();
		while (iterator.hasNext()) {
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
		int v0 = (header[1] & 0xff);
		int v1 = (header[2] & 0xff) << 8;
		int v2 = (header[3] & 0xff) << 16;
		int v3 = (header[4] & 0xff) << 24;
		return v0 | v1 | v2 | v3;
	}

	private ByteBuffer read(EndPoint endPoint, int length, long timeout) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(length);
		int _length = endPoint.read(buffer);
		while (_length < length) {
			int __length = endPoint.read(buffer);
			_length += __length;
		}
		return buffer;
	}

	public void setNetworkWeak() {
		this.netweak = true;
	}

	public void wakeup() {
		this.selector.wakeup();
	}

	public void write(String sessionID, String serviceName, String content) throws IOException {
		checkConnector();
		Selector selector = this.selector;
		// endPoint.register(selector, SelectionKey.OP_WRITE);

		Charset charset = Encoding.DEFAULT;

		ByteBuffer buffer = null;

		if (StringUtil.isNullOrBlank(content)) {
			buffer = ProtocolEncoder.encode(sessionID.getBytes(charset), serviceName.getBytes(charset));
		} else {
			buffer = ProtocolEncoder.encode(sessionID.getBytes(charset), serviceName.getBytes(charset),
					content.getBytes(charset));
		}
		buffer.flip();
		ClientEndPoint endPoint = this.endPoint;
		writer.writeText(endPoint, buffer);
		endPoint.register(selector, SelectionKey.OP_READ);
	}

	public void write(String sessionID, String serviceName, String content, InputStream inputStream)
			throws IOException {

		if (inputStream == null) {
			this.write(sessionID, serviceName, content);
		} else {
			checkConnector();
			ClientEndPoint endPoint = this.endPoint;
			Selector selector = this.selector;
			// endPoint.register(selector, SelectionKey.OP_WRITE);
			int avaiable = inputStream.available();
			Charset charset = Encoding.DEFAULT;

			ByteBuffer buffer = null;

			if (StringUtil.isNullOrBlank(content)) {
				buffer = ProtocolEncoder.encode(sessionID.getBytes(charset), serviceName.getBytes(charset),
						avaiable);
			} else {
				buffer = ProtocolEncoder.encode(sessionID.getBytes(charset), serviceName.getBytes(charset),
						content.getBytes(charset), avaiable);
			}

			buffer.flip();
			
			writer.writeStream(endPoint, inputStream, buffer, BLOCK);

//			synchronized (writeLock) {
//				endPoint.write(buffer);
//				byte[] bytes = new byte[BLOCK];
//				int length = inputStream.read(bytes);
//				while (length == BLOCK) {
//					buffer = ByteBuffer.wrap(bytes);
//					endPoint.write(buffer);
//					length = inputStream.read(bytes);
//				}
//				if (length > 0) {
//					buffer = ByteBuffer.wrap(bytes, 0, length);
//					endPoint.write(buffer);
//				}
//			}
			endPoint.register(selector, SelectionKey.OP_READ);
		}

	}
	
	public void writeBeat() throws IOException {
		checkConnector();
		writer.writeBeat(endPoint);
		System.out.println(">>write beat........."+DateUtil.now());
	}
	
	public void keepAlive(){
		this.writer = writers[1];
	}
	
	private static ClientWriter [] writers = new ClientWriter[]{new NormalClientWriter(),new AliveClientWriter()};

}
