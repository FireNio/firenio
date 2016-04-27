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
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.server.NIOContext;

public class NIOEndPoint implements EndPoint {

	private Attachment			attachment		= null;
	private int				attempts			= 0;
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
//	private ReentrantLock		lock				= new ReentrantLock();
	private IOWriteFuture		currentWriter		= null;

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
		
//		ReentrantLock lock = this.lock;
//		
//		lock.lock();
		_currentPusher.push(writer);
		
//		lock.unlock();
	}

	public void attach(Attachment attachment) {
		this.attachment = attachment;
	}

	public Attachment attachment() {
		return attachment;
	}

	public void attackNetwork(int length) {
		if (length == 0) {
			attempts++;
			return;
		}
		attempts = 0;
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
	
	interface Pusher{
		
		void push(IOWriteFuture future);
	}
	
	private Pusher _localPusher = new Pusher() {
		
		public void push(IOWriteFuture future) {
			writers.add(future);
		}
	};
	
	private Pusher _remotePusher = new Pusher() {
		
		public void push(IOWriteFuture future) {
			context.getEndPointWriter().offer(future);
		}
	};
	
	private Pusher _currentPusher = _localPusher;
	
	public void flushWriters() throws IOException {
		
//		ReentrantLock lock = this.lock;
//		
//		lock.lock();
		
		if (this.currentWriter == null) {
			
			flushWriters0();
			
		}else if (this.currentWriter.write()) {
			
			this.setWriting(0);
			
			flushWriters0();
			
		}
		
//		lock.unlock();
		
	}
	
	public void flushWriters0() throws IOException {
			
		_currentPusher = _remotePusher;
		
//			this._networkWeak = false;
		
		this.currentWriter = null;
		
		List<IOWriteFuture> writers = this.writers;

		EndPointWriter endPointWriter = context.getEndPointWriter();

		for (IOWriteFuture writer : writers) {
			endPointWriter.offer(writer);
		}

		writers.clear();
		
		selectionKey.interestOps(SelectionKey.OP_READ);
		
		attempts = 0;
		
		_currentPusher = _localPusher;
	}
	
	public void interestWrite() {
		selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public boolean isEndConnect() {
		return endConnect;
	}

	public boolean isNetworkWeak() {
		return attempts > 16;
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
	
	public void setCurrentWriter(IOWriteFuture writer) {
		this.currentWriter = writer;
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
		return "remote /"+this.getRemoteHost() + "("+this.getRemoteAddr()+ "):" + this.getRemotePort() + " lis-"+selectionKey.interestOps();
	}
	
}
