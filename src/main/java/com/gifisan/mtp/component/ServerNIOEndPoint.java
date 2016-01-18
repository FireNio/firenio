package com.gifisan.mtp.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;
import com.gifisan.mtp.server.session.InnerSession;

public class ServerNIOEndPoint implements ServerEndPoint {

	private SelectionKey		selectionKey		= null;
	private Object				attachment		= null;
	private SocketChannel		channel			= null;
	private int				comment			= 0;
	private boolean			endConnect		= false;
	private MTPRequestInputStream	inputStream		= null;
	private InetSocketAddress	local			= null;
	private int				maxIdleTime		= 0;
	private ProtocolDecoder		protocolDecoder	= null;
	private InetSocketAddress	remote			= null;
	private Socket				socket			= null;
	private InnerSession		session			= null;

	public ServerNIOEndPoint(SelectionKey selectionKey) throws SocketException {
		this.selectionKey = selectionKey;
		this.protocolDecoder = new NormalProtocolDecoder();
		this.channel = (SocketChannel) selectionKey.channel();
		socket = channel.socket();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}
		maxIdleTime = socket.getSoTimeout();
	}

	public void attach(Object attachment) {
		this.attachment = attachment;
	}

	public Object attachment() {
		return attachment;
	}

	public void close() throws IOException {
		this.selectionKey.attach(null);
		this.session.destroyImmediately();
		this.channel.close();
	}

	public int comment() {
		return this.comment;
	}

	public ByteBuffer completeRead(int limit) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(limit);
		int time = limit / 64;
		int _time = 0;
		int length = -1;
		try {
			length = channel.read(buffer);
			while (length < limit && _time < time) {
				int _length = channel.read(buffer);
				length += _length;
				_time++;
			}
		} catch (IOException e) {
			throw handleException(e);
		}
		if (length < limit) {
			throw new MTPChannelException("network is too weak");
		}
		return buffer;
	}

	public void endConnect() {
		this.endConnect = true;
		// System.out.println("end connect!!!!!!!!!!!!!!!!!!!!!!!");
	}

	public MTPRequestInputStream getInputStream() {
		return inputStream;
	}

	public String getLocalAddr() {
		if (local == null) {
			local = (InetSocketAddress) socket.getLocalSocketAddress();
		}
		return local.getAddress().getCanonicalHostName();
	}

	public String getLocalHost() {
		return local.getHostName();
	}

	public int getLocalPort() {
		return local.getPort();
	}

	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return this.protocolDecoder;
	}

	public String getRemoteAddr() {
		if (remote == null) {
			remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		}
		return remote.getAddress().getCanonicalHostName();
	}

	public String getRemoteHost() {
		if (remote == null) {
			remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		}
		return remote.getAddress().getHostName();
	}

	public int getRemotePort() {
		if (remote == null) {
			remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		}
		return remote.getPort();
	}

	private MTPChannelException handleException(IOException exception) throws MTPChannelException {
		this.endConnect = true;

		return new MTPChannelException(exception.getMessage(), exception);
	}

	public boolean inStream() {
		return inputStream != null && !inputStream.complete();
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public boolean isEndConnect() {
		return endConnect;
	}

	public boolean isOpened() {
		return this.channel.isOpen();
	}

	public boolean protocolDecode(ServletContext context) throws IOException {
		this.protocolDecoder = new NormalProtocolDecoder();
		return this.protocolDecoder.decode(context, this);
	}

	public int read(ByteBuffer buffer) throws IOException {
		try {
			return this.channel.read(buffer);
		} catch (IOException e) {
			throw handleException(e);
		}
	}

	public ByteBuffer read(int limit) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(limit);
		int length = -1;
		try {
			length = channel.read(buffer);
		} catch (IOException e) {
			throw handleException(e);
		}
		if (length < limit) {
			throw new MTPChannelException("network is too weak");
		}
		return buffer;
	}

	public int readHead(ByteBuffer buffer) throws IOException {
		return this.read(buffer);
	}

	public void setComment(int comment) {
		this.comment = comment;

	}
	
	public InnerSession getSession() {
		return session;
	}

	public void setSession(InnerSession session) {
		this.session = session;
		
	}

	public void setMTPRequestInputStream(MTPRequestInputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void write(byte b) throws MTPChannelException {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		buffer.put(b);
		write(buffer);
	}

	public void write(byte[] bytes) throws MTPChannelException {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		write(buffer);
	}

	public void write(byte[] bytes, int offset, int length) throws MTPChannelException {
		ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
		write(buffer);
	}

	public void write(ByteBuffer buffer) throws MTPChannelException {
		write(channel, buffer);
	}

	// TODO 网速比较慢的时候
	private void write(SocketChannel client, ByteBuffer buffer) throws MTPChannelException {
		try {
			int length = buffer.limit();
			int _length = client.write(buffer);
			while (length > _length) {
				_length += client.write(buffer);
			}
		} catch (IOException e) {
			throw handleException(e);
		}
	}

}
