package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.DefaultEndPointWriter.EndPointWriteEvent;
import com.gifisan.nio.component.protocol.future.IOReadFuture;
import com.gifisan.nio.component.protocol.future.IOWriteFuture;

public class DefaultTCPEndPoint extends AbstractEndPoint implements TCPEndPoint {

	private static final Logger	logger			= LoggerFactory.getLogger(DefaultTCPEndPoint.class);
	private AtomicBoolean		_closed			= new AtomicBoolean(false);
	private boolean			_networkWeak;
	private SocketChannel		channel;
	private IOWriteFuture		currentWriter;
	private boolean			endConnect;
	private EndPointWriter		endPointWriter;
	private IOReadFuture		readFuture;
	private SelectionKey		selectionKey;
	private Session			session;
	private Socket				socket;
	private AtomicInteger		writers			= new AtomicInteger();
	private long				next_network_weak	= Long.MAX_VALUE;

	// FIXME network weak check
	public DefaultTCPEndPoint(NIOContext context, SelectionKey selectionKey, EndPointWriter endPointWriter)
			throws SocketException {
		super(context);
		this.selectionKey = selectionKey;
		this.endPointWriter = endPointWriter;
		this.channel = (SocketChannel) selectionKey.channel();
		this.socket = channel.socket();
		// FIXME 检查这行代码是否可以解决远程访问服务时卡顿问题
		this.local = getLocalSocketAddress();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}

		this.session = new IOSession(this, getEndPointID());

		SessionEventListenerWrapper listenerWrapper = context.getSessionEventListenerStub();

		for (; listenerWrapper != null;) {
			try {
				listenerWrapper.sessionOpened(session);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			listenerWrapper = listenerWrapper.nextListener();
		}
	}

	//FIXME synch it ? 
	public void updateNetworkState(int length) {

		if (length == 0) {
			if (next_network_weak < Long.MAX_VALUE) {
				
				if (System.currentTimeMillis() > next_network_weak) {
					
					if (!_networkWeak) {

						_networkWeak = true;
						
						interestWrite();
					}
				}
			}else{
				
				next_network_weak = System.currentTimeMillis() + 64;
			}
		}else{
			
			if (next_network_weak != Long.MAX_VALUE) {
				
				next_network_weak = Long.MAX_VALUE;
				
				_networkWeak = false;
			}
		}
	}

	public void close() throws IOException {

		if (writers.get() > 0) {
			return;
		}

		if (_closed.compareAndSet(false, true)) {

			this.endConnect = true;

//			this.selectionKey.attach(null);

			Session session = getSession();

			getContext().getSessionFactory().removeSession(session);

			session.destroy();

			this.channel.close();
		}
	}

	public void decrementWriter() {
		writers.decrementAndGet();
	}

	public void endConnect() {
		this.endConnect = true;
	}

	public void wakeup() throws IOException {

		this.endPointWriter.fire(new EndPointWriteEvent() {
			
			public void handle(EndPointWriter endPointWriter) {
				
				DefaultTCPEndPoint endPoint = DefaultTCPEndPoint.this;
				
				endPoint.updateNetworkState(1);
				
				endPointWriter.wekeupEndPoint(endPoint);
				
//				logger.debug("EndPoint wakeup:{}",endPoint);
			}
		});

		this.selectionKey.interestOps(SelectionKey.OP_READ);
	}

	public IOWriteFuture getCurrentWriter() {
		return currentWriter;
	}

	public EndPointWriter getEndPointWriter() {
		return endPointWriter;
	}

	public InetSocketAddress getLocalSocketAddress() {
		if (local == null) {
			local = (InetSocketAddress) socket.getLocalSocketAddress();
		}
		return local;
	}

	protected String getMarkPrefix() {
		return "TCP";
	}

	public int getMaxIdleTime() throws SocketException {
		return socket.getSoTimeout();
	}

	public IOReadFuture getReadFuture() {
		return readFuture;
	}

	public InetSocketAddress getRemoteSocketAddress() {
		if (remote == null) {
			remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		}
		return remote;
	}

	public Session getSession() {
		return session;
	}

	public void incrementWriter() {
		writers.incrementAndGet();
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

	public void setCurrentWriter(IOWriteFuture writer) {
		this.currentWriter = writer;
	}

	public void setReadFuture(IOReadFuture readFuture) {
		this.readFuture = readFuture;
	}

	public int write(ByteBuffer buffer) throws IOException {
		return channel.write(buffer);
	}

}
