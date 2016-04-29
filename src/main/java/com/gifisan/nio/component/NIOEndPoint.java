package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.NetworkException;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.server.NIOContext;

public class NIOEndPoint implements EndPoint {

	private Attachment			attachment	= null;
	private int				attempts		= 0;
	private SocketChannel		channel		= null;
	private NIOContext			context		= null;
	private boolean			endConnect	= false;
	private InetSocketAddress	local		= null;
	private InetSocketAddress	remote		= null;
	private SelectionKey		selectionKey	= null;
	private Session[]			sessions		= new Session[4];
	private Socket				socket		= null;
//	private List<IOWriteFuture>	writers		= new ArrayList<IOWriteFuture>();
	private SessionFactory		sessionFactory	= null;
	private IOReadFuture		readFuture	= null;
	private long				_futureID		= 0;
//	 private ReentrantLock 		lock 		= new ReentrantLock();
	private IOWriteFuture		currentWriter	= null;
	private AtomicBoolean		_closed		= new AtomicBoolean(false);
	private Long				endPointID	= null;
	private boolean 			_networkWeak	= false;
	private static AtomicLong	autoEndPointID = new AtomicLong(10000);
	

	public NIOEndPoint(NIOContext context, SelectionKey selectionKey) throws SocketException {
		this.context = context;
		this.selectionKey = selectionKey;
		this.channel = (SocketChannel) selectionKey.channel();
		// this.channel = channel;
		this.sessionFactory = context.getSessionFactory();
		this.socket = channel.socket();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}
		this.endPointID = autoEndPointID.getAndIncrement();
	}

//	public void addWriter(IOWriteFuture writer) throws IOException {
//
////		ReentrantLock lock = this.lock;
//		//
////		lock.lock();
//		
////		if (isNetworkWeak()) {
////			this._localPusher.push(writer);
////		}else{
////			this._remotePusher.push(writer);
////		}
//		
//		_currentPusher.push(writer);
//		
////		lock.unlock();
//	}

	public void attach(Attachment attachment) {
		this.attachment = attachment;
	}

	public Attachment attachment() {
		return attachment;
	}

	public void attackNetwork(int length) {
		
		if (length == 0) {
			if (_networkWeak) {
				return;
			}
			
			if (++attempts > 255) {
				this.interestWrite();
				_networkWeak = true;
			}
			return;
		}
		attempts = 0;
	}

	public boolean enableWriting(long futureID) {
		return (_futureID == 0) || (_futureID == futureID);
	}

	public void close() throws IOException {
		if (_closed.compareAndSet(false, true)) {
			
			this.endConnect = true;
			
			this.selectionKey.attach(null);

			DebugUtil.debug(">>>>>> rm "+this.toString());

			for (Session session : sessions) {
				if (session == null) {
					continue;
				}
				session.destroyImmediately();
			}

			this.channel.close();
			
			this.extendClose();
			
		}
	}
	
	protected void extendClose(){}

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
			throw new IOException("invalid session id " + sessionID);
		}

		Session session = sessions[sessionID];

		if (session == null) {
			session = sessionFactory.getSession(this, sessionID);
			sessions[sessionID] = session;
		}

		return session;
	}

//	interface Pusher {
//
//		void push(IOWriteFuture future) throws IOException;
//	}
//
//	private Pusher	_localPusher	= new Pusher() {
//
//		public void push(IOWriteFuture future) {
//			writers.add(future);
//		}
//	};
//
//	private Pusher	_remotePusher	= new Pusher() {
//
//		public void push(IOWriteFuture future) throws IOException {
//			if (!context.getEndPointWriter().offer(future)) {
//				future.catchException(WriterOverflowException.INSTANCE);
//			}
//		}
//	};

//	private Pusher	_currentPusher	= _localPusher;

	public void flushWriters() throws IOException {
//		this._currentPusher = _remotePusher;
		
//		List<IOWriteFuture> writers = this.writers;

		EndPointWriter endPointWriter = context.getEndPointWriter();

		// ReentrantLock lock = this.lock;
		//
		// lock.lock();

		if (this.currentWriter == null) {
			this.flushWriters0(endPointWriter);
		}else{
			
			if(this.currentWriter.write()){
				this.currentWriter = null;
				this.setWriting(0);
				this.flushWriters0(endPointWriter);
			 }else{
				 return;
			 }
		}

//		for (IOWriteFuture writer : writers) {
//			if (!endPointWriter.offer(writer)) {
//				writer.catchException(WriterOverflowException.INSTANCE);
//			}
//		}

//		writers.clear();

//		_currentPusher = _localPusher;

		// lock.unlock();

	}
	
	private void flushWriters0(EndPointWriter endPointWriter){
		this.attempts = 0;
		this._networkWeak = false;
		endPointWriter.collect();
		selectionKey.interestOps(SelectionKey.OP_READ);
		
		
	}

	private void interestWrite() {
		selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public boolean isEndConnect() {
		return endConnect;
	}

	public boolean isNetworkWeak() {
		return _networkWeak;
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
			throw new NetworkException("poor network ");
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
		return new StringBuilder("EDP[id:")
				.append(endPointID)
				.append("] remote /")
				.append(this.getRemoteHost())
				.append("(")
				.append(this.getRemoteAddr())
				.append("):")
				.append(this.getRemotePort())
				.toString();
	}

	public Long getEndPointID() {
		return endPointID;
	}

}
