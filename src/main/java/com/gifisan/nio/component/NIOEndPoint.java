package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.client.IOWriteFuture;
import com.gifisan.nio.server.NIOContext;

public class NIOEndPoint implements EndPoint {

	private Attachment			attachment		= null;
	private boolean			attempts0			= false;
	private boolean			attempts1			= false;
	private SocketChannel		channel			= null;
	private NIOContext			context			= null;
	private boolean			endConnect		= false;
	private InetSocketAddress	local			= null;
	private InetSocketAddress	remote			= null;
	private SelectionKey		selectionKey		= null;
	private Session[]			sessions			= new Session[4];
	private Socket				socket			= null;
	private List<IOWriteFuture>	writers			= new ArrayList<IOWriteFuture>();
	private SessionFactory		sessionFactory		= null;
	private IOReadFuture 		readFuture 		= null;
	private long				_futureID			= 0;

	public NIOEndPoint(NIOContext context,SelectionKey selectionKey) throws SocketException {
		this.context = context;
		this.selectionKey = selectionKey;
		this.channel = (SocketChannel) selectionKey.channel();
//		this.channel = channel;
		this.sessionFactory = context.getSessionFactory();
		this.socket = channel.socket();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}
	}

	public void addWriter(IOWriteFuture writer) {
		if (isNetworkWeak()) {
			writers.add(writer);
		} else {
			context.getEndPointWriter().offer(writer);
		}
	}

	public void attach(Attachment attachment) {
		this.attachment = attachment;
	}

	public Attachment attachment() {
		return attachment;
	}

	public void attackNetwork(int length) {
		if (attempts0) {
			attempts1 = length == 0;
			return;
		}

		attempts0 = length == 0;
	}

	public boolean enableWriting(long futureID) {
		return (_futureID == 0) || (_futureID == futureID);
	}

	public void close() throws IOException {
		this.selectionKey.attach(null);

		for (Session session : sessions) {
			if (session == null) {
				continue;
			}
			session.destroyImmediately();
		}

		this.channel.close();
	}

	public void endConnect() {
		this.endConnect = true;
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

	public int getMaxIdleTime() throws SocketException {
		return socket.getSoTimeout();
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

	public Session getSession(byte sessionID) throws IOException {
	
		if (sessionID > 3 || sessionID < 0) {
			throw new IOException("invalid session id "+sessionID);
		}
		
		Session session = sessions[sessionID];

		if (session == null) {
			session = sessionFactory.getSession(this, sessionID);
			sessions[sessionID] = session;
		}

		return session;
	}

	public List<IOWriteFuture> getWriter() {
		this.attempts0 = false;
		this.attempts1 = false;
		return writers;
	}

	public void interestWrite() {
		selectionKey.interestOps(SelectionKey.OP_WRITE);
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public boolean isEndConnect() {
		return endConnect;
	}

	public boolean isNetworkWeak() {
		return attempts1;
	}

	public boolean isOpened() {
		return this.channel.isOpen();
	}

	public int read(ByteBuffer buffer) throws IOException {
		return this.channel.read(buffer);
	}

	public ByteBuffer read(int limit) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(limit);
		this.read(buffer);
		if (buffer.position() < limit) {
			throw new IOException("poor network ");
		}
		return buffer;
	}

	public void removeSession(byte sessionID) {
		Session session = sessions[sessionID];

		sessions[sessionID] = null;
		if (session != null) {
			session.destroyImmediately();
		}
	}

	public void setWriting(long futureID) {
		this._futureID = futureID;
	}

	public int write(ByteBuffer buffer) throws IOException {
		return channel.write(buffer);
	}

	public NIOContext getContext() {
		return context;
	}

	public IOReadFuture getReadFuture() {
		return readFuture;
	}

	public void setReadFuture(IOReadFuture readFuture) {
		this.readFuture = readFuture;
	}

	public String toString() {
		return "remote /"+this.getRemoteHost() + "("+this.getRemoteAddr()+ "):" + this.getRemotePort();
	}
	
}
