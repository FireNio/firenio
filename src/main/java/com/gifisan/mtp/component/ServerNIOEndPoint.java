package com.gifisan.mtp.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.gifisan.mtp.server.ServerContext;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServerEndpointFactory;
import com.gifisan.mtp.server.session.InnerSession;
import com.gifisan.mtp.server.session.MTPSession;

public class ServerNIOEndPoint implements ServerEndPoint {

	private Object				attachment		= null;
	private SocketChannel		channel			= null;
	private ServerContext		context			= null;
	private boolean			endConnect		= false;
	private long				endPointID		= 0;
	private ServerEndpointFactory	factory			= null;
	private InputStream			inputStream		= null;
	private InetSocketAddress	local			= null;
	private int				mark				= 0;
	private int				maxIdleTime		= 0;
	private ProtocolDecoder		protocolDecoder	= null;
	private InetSocketAddress	remote			= null;
	private SelectionKey		selectionKey		= null;
	private InnerSession[]		sessions			= new InnerSession[4];
	private Socket				socket			= null;
	private int				sessionSize		= 0;

	public ServerNIOEndPoint(ServerContext context, SelectionKey selectionKey, long endPointID) throws SocketException {
		this.context = context;
		this.factory = context.getServerEndpointFactory();
		this.endPointID = endPointID;
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

		for (InnerSession session : sessions) {
			if (session == null) {
				continue;
			}
			session.destroyImmediately();
		}

		this.factory.remove(this);

		this.channel.close();

	}

	public void endConnect() {
		this.endConnect = true;
	}

	public ServerContext getContext() {
		return context;
	}

	public long getEndPointID() {
		return endPointID;
	}

	public InputStream getInputStream() {
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

	public int getMark() {
		return mark;
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

	public InnerSession getSession() {

		byte sessionID = protocolDecoder.getSessionID();

		InnerSession session = sessions[sessionID];

		if (session == null) {
			session = new MTPSession(this, sessionID);
			sessions[sessionID] = session;
			sessionSize = sessionID;
		}

		return session;
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

	public boolean protocolDecode(ServerContext context) throws IOException {
		this.protocolDecoder = new NormalProtocolDecoder();
		return this.protocolDecoder.decode(this);
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

		SocketChannel channel = this.channel;

		try {

			int _length = channel.read(buffer);
			int length = _length;

			for (; length < limit;) {
				if (length < 64) {
					throw new MTPChannelException("network is weak");
				}
				_length = channel.read(buffer);
				length += _length;
			}

		} catch (IOException e) {
			throw handleException(e);
		}
		return buffer;
	}

	public void removeSession(byte sessionID) {
		InnerSession session = sessions[sessionID];

		sessions[sessionID] = null;
		if (session != null) {
			session.destroyImmediately();
		}
	}

	public int sessionSize() {
		return sessionSize;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void setMark(int mark) {
		this.mark = mark;
	}

	public void write(byte b) throws MTPChannelException {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		buffer.put(b);
		write(buffer);
	}

	public void write(byte[] bytes) throws MTPChannelException {
		write(ByteBuffer.wrap(bytes));
	}

	public void write(byte[] bytes, int offset, int length) throws MTPChannelException {
		write(ByteBuffer.wrap(bytes, offset, length));
	}

	public void write(ByteBuffer buffer) throws MTPChannelException {
		write(channel, buffer);
	}

	// TODO 网速比较慢的时候
	private void write(SocketChannel channel, ByteBuffer buffer) throws MTPChannelException {

		int limit = buffer.limit();

		try {

			int _length = channel.write(buffer);
			int length = _length;

			for (; length < limit;) {
				// TODO 处理网速较慢的时候
				_length = channel.write(buffer);
				length += _length;
			}

		} catch (IOException e) {
			throw handleException(e);
		}

	}

}
