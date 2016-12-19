package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.ClosedChannelException;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueLink;
import com.generallycloud.nio.connector.ChannelConnector;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.SslReadFuture;

public class NioSocketChannel extends AbstractChannel implements com.generallycloud.nio.component.SocketChannel {

	private Socket						socket;
	private SocketChannel				channel;
	private UnsafeSocketSession			session;
	private ChannelReadFuture			readFuture;
	private SslReadFuture				sslReadFuture;
	private SelectionKey				selectionKey;
	private boolean					networkWeak;
	private ProtocolDecoder				protocolDecoder;
	private ProtocolEncoder				protocolEncoder;
	private ProtocolFactory				protocolFactory;
	private SocketChannelContext			context;
	private ChannelWriteFuture			writeFuture;
	private long						next_network_weak	= Long.MAX_VALUE;
	private boolean					enableInbound		= true;
	private int						writeFutureLength	;
	private EventLoop					eventLoop;

	// FIXME 这里最好不要用ABQ，使用链式可增可减
	private ListQueue<ChannelWriteFuture>	writeFutures		= new ListQueueLink<ChannelWriteFuture>();
	
	private static final Logger logger = LoggerFactory.getLogger(NioSocketChannel.class);

	// FIXME 改进network wake 机制
	// FIXME network weak check
	public NioSocketChannel(SocketChannelSelectorLoop selectorLoop, SelectionKey selectionKey) throws SocketException {
		super(selectorLoop);
		this.context = selectorLoop.getContext();
		this.selectionKey = selectionKey;
		this.eventLoop = selectorLoop.getEventLoop();
		this.channel = (SocketChannel) selectionKey.channel();
		this.socket = channel.socket();
		this.local = getLocalSocketAddress();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}
		
		this.protocolFactory = selectorLoop.getProtocolFactory();
		this.protocolDecoder = selectorLoop.getProtocolDecoder();
		this.protocolEncoder = selectorLoop.getProtocolEncoder();
		this.session = context.getSessionFactory().newUnsafeSession(this);
	}

	@Override
	public void close() throws IOException {

		ReentrantLock lock = this.channelLock;

		lock.lock();

		try {

			if (!opened) {
				return;
			}

			if (inSelectorLoop()) {

				this.session.physicalClose();

				this.physicalClose();

			} else {

				if (closing) {
					return;
				}
				closing = true;

				fireClose();
			}
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public SocketChannelContext getContext() {
		return context;
	}

	private void fireClose() {

		fireEvent(new SelectorLoopEventAdapter() {

			@Override
			public boolean handle(SelectorLoop selectLoop) throws IOException {

				CloseUtil.close(NioSocketChannel.this);

				return false;
			}
		});
	}

	@Override
	public boolean handle(SelectorLoop selectorLoop) throws IOException {

		if (!isOpened()) {
			throw new ClosedChannelException("closed");
		}

		if (writeFuture == null) {
			writeFuture = writeFutures.poll();
		}

		if (writeFuture == null) {
			return false;
		}

		if (!writeFuture.write(this)) {
			return true;
		}
		
		writeFutureLength -= writeFuture.getBinaryLength();

		writeFuture.onSuccess(session);

		writeFuture = null;

		return needFlush();
	}
	
	public int getWriteFutureLength() {
		return writeFutureLength;
	}

	@Override
	public ChannelWriteFuture getWriteFuture() {
		return writeFuture;
	}

	@Override
	public InetSocketAddress getLocalSocketAddress() {
		if (local == null) {
			local = (InetSocketAddress) socket.getLocalSocketAddress();
		}
		return local;
	}

	@Override
	protected String getMarkPrefix() {
		return "TCP";
	}

	@Override
	public int getMaxIdleTime() throws SocketException {
		return socket.getSoTimeout();
	}

	@Override
	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	@Override
	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	@Override
	public ChannelReadFuture getReadFuture() {
		return readFuture;
	}

	@Override
	public InetSocketAddress getRemoteSocketAddress() {
		if (remote == null) {
			remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		}
		return remote;
	}

	@Override
	public UnsafeSocketSession getSession() {
		return session;
	}

	@Override
	public int getWriteFutureSize() {
		return writeFutures.size();
	}

	@Override
	public boolean isBlocking() {
		return channel.isBlocking();
	}

	@Override
	public boolean isNetworkWeak() {
		return networkWeak;
	}

	// FIXME 是否使用channel.isOpen()
	@Override
	public boolean isOpened() {
		return opened;
	}

	@Override
	public void offer(ChannelWriteFuture future) {

		ReentrantLock lock = channelLock;

		lock.lock();

		try {

			if (!enableInbound) {

				future.onException(session, new ClosedChannelException(session.toString()));

				return;
			}

			if (!writeFutures.offer(future)) {

				future.onException(session, new RejectedExecutionException());

				return;
			}
			
			this.writeFutureLength += future.getBinaryLength();
			
			if (writeFutureLength > 1024 * 1024 * 10) {
				//FIXME 该连接写入过多啦
			}

			if (writeFutures.size() > 1) {

				return;
			}

			selectorLoop.fireEvent(this);

		} finally {

			lock.unlock();
		}
	}

	private void releaseWriteFutures() {

		ClosedChannelException e = null;

		if (writeFuture != null) {

			e = new ClosedChannelException(session.toString());

			ReleaseUtil.release(writeFuture);

			writeFuture.onException(session, e);
		}

		ListQueue<ChannelWriteFuture> writeFutures = this.writeFutures;

		if (writeFutures.size() == 0) {
			return;
		}

		ChannelWriteFuture f = writeFutures.poll();

		UnsafeSocketSession session = this.session;

		if (e == null) {
			e = new ClosedChannelException(session.toString());
		}

		for (; f != null;) {

			f.onException(session, e);

			ReleaseUtil.release(f);

			f = writeFutures.poll();
		}
	}

	// FIXME 这里有问题
	@Override
	public void physicalClose() {

		this.enableInbound = false;

		ReleaseUtil.release(readFuture);
		ReleaseUtil.release(sslReadFuture);

		// 最后一轮 //FIXME once
		try {
			this.handle(selectorLoop);
		} catch (IOException e) {
		}

		this.releaseWriteFutures();

		this.selectionKey.attach(null);

		try {
			this.channel.close();
		} catch (Exception e) {
		}

		this.opened = false;

		this.closing = false;

		this.selectionKey.cancel();

		ChannelService service = context.getChannelService();

		if (service instanceof ChannelConnector) {

			try {
				((ChannelConnector) service).physicalClose();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public int read(ByteBuffer buffer) throws IOException {
		return this.channel.read(buffer);
	}

	@Override
	public void setWriteFuture(ChannelWriteFuture future) {
		this.writeFuture = future;
	}

	@Override
	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		this.protocolDecoder = protocolDecoder;
	}

	@Override
	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		this.protocolEncoder = protocolEncoder;
	}

	@Override
	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		this.protocolFactory = protocolFactory;
	}

	@Override
	public void setReadFuture(ChannelReadFuture readFuture) {
		this.readFuture = readFuture;
	}

	@Override
	public void upNetworkState() {

		if (next_network_weak != Long.MAX_VALUE) {

			next_network_weak = Long.MAX_VALUE;

			networkWeak = false;
		}
	}

	@Override
	public void downNetworkState() {
		
		long current = System.currentTimeMillis();

		if (next_network_weak < Long.MAX_VALUE) {
			
			if (networkWeak) {
				return;
			}
			
			if (current > next_network_weak) {
				
				networkWeak = true;
				
				fireEvent(this);
			}

		} else {

			next_network_weak = current + 64;
		}
		
	}

	@Override
	public int write(ByteBuffer buffer) throws IOException {
		return channel.write(buffer);
	}

	@Override
	public SslReadFuture getSslReadFuture() {
		return sslReadFuture;
	}

	@Override
	public void setSslReadFuture(SslReadFuture future) {
		this.sslReadFuture = future;
	}

	@Override
	public boolean needFlush() {
		return writeFuture != null || writeFutures.size() > 0;
	}

	@Override
	public void fireEvent(SelectorLoopEvent event) {
		this.selectorLoop.fireEvent(event);
	}

	@Override
	public boolean isPositive() {
		return !isNetworkWeak();
	}

	@Override
	public EventLoop getEventLoop() {
		return eventLoop;
	}
	
}
