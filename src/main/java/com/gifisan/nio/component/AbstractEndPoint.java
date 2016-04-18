package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.gifisan.nio.server.session.InnerSession;

public abstract class AbstractEndPoint implements EndPoint {

	private SlowlyNetworkReader	accept			= null;
	protected SocketChannel		channel			= null;
	private InnerSession		currentSession		= null;
	private boolean			endConnect		= false;
	private InetSocketAddress	local			= null;
	private int				maxIdleTime		= 0;
	private InetSocketAddress	remote			= null;
	private Socket				socket			= null;
	private byte				writingSessionID	= -1;

	public AbstractEndPoint(SelectionKey selectionKey) throws SocketException {
		this.channel = (SocketChannel) selectionKey.channel();
		socket = channel.socket();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}
		maxIdleTime = socket.getSoTimeout();
	}

	public void close() throws IOException {
		this.channel.close();
	}

	public void endConnect() {
		this.endConnect = true;
	}

	public InnerSession getCurrentSession() {
		return currentSession;
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

	public SlowlyNetworkReader getSchedule() {
		return accept;
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

	public boolean isWriting(byte sessionID) {
		return writingSessionID == sessionID;
	}

	public int read(ByteBuffer buffer) throws IOException {
		return this.channel.read(buffer);
	}

	public void setCurrentSession(InnerSession session) {
		this.currentSession = session;
	}

	public void setSchedule(SlowlyNetworkReader accept) {
		this.accept = accept;
	}

	public void setWriting(byte sessionID) {
		this.writingSessionID = sessionID;
	}

	public int write(ByteBuffer buffer) throws IOException {
		return channel.write(buffer);
	}

	public ByteBuffer read(int limit) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(limit);
		this.read(buffer);
		if (buffer.limit() < limit) {
			throw new IOException("poor network ");
		}
		return buffer;
	}
	
}
