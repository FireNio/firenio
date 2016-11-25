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
	private boolean					opened			= true;
	private boolean					closing			= false;
	private long						next_network_weak	= Long.MAX_VALUE;
	private boolean					enableInbound		= true;

	// FIXME 这里最好不要用ABQ，使用链式可增可减
	private ListQueue<ChannelWriteFuture>	writeFutures		= new ListQueueLink<ChannelWriteFuture>();
	
	private static final Logger logger = LoggerFactory.getLogger(NioSocketChannel.class);

	// FIXME 改进network wake 机制
	// FIXME network weak check
	public NioSocketChannel(SocketChannelSelectorLoop selectorLoop, SelectionKey selectionKey) throws SocketException {
		super(selectorLoop.getContext(),selectorLoop.getByteBufAllocator());
		this.context = selectorLoop.getContext();
		this.selectionKey = selectionKey;
		this.selectorLoop = selectorLoop;
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

	public void close() throws IOException {

		ReentrantLock lock = this.channelLock;

		lock.lock();

		try {

			if (!opened) {
				return;
			}

			if (isInSelectorLoop()) {

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
	
	public SocketChannelContext getContext() {
		return context;
	}

	private void fireClose() {

		fireEvent(new SelectorLoopEventAdapter() {

			public boolean handle(SelectorLoop selectLoop) throws IOException {

				CloseUtil.close(NioSocketChannel.this);

				return true;
			}
		});
	}

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
			return !isNetworkWeak();
		}

		writeFuture.onSuccess(session);

		writeFuture = null;

		return needFlush();
	}

	public ChannelWriteFuture getWriteFuture() {
		return writeFuture;
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

	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	public ChannelReadFuture getReadFuture() {
		return readFuture;
	}

	public InetSocketAddress getRemoteSocketAddress() {
		if (remote == null) {
			remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		}
		return remote;
	}

	public UnsafeSocketSession getSession() {
		return session;
	}

	public int getWriteFutureSize() {
		return writeFutures.size();
	}

	private void interestWrite() {
		selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public boolean isNetworkWeak() {
		return networkWeak;
	}

	// FIXME 是否使用channel.isOpen()
	public boolean isOpened() {
		return opened;
	}

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
	public void physicalClose() {

		this.enableInbound = false;

		ReleaseUtil.release(readFuture);
		ReleaseUtil.release(sslReadFuture);

		// 最后一轮
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

	public int read(ByteBuffer buffer) throws IOException {
		return this.channel.read(buffer);
	}

	public void setWriteFuture(ChannelWriteFuture future) {
		this.writeFuture = future;
	}

	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		this.protocolDecoder = protocolDecoder;
	}

	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		this.protocolEncoder = protocolEncoder;
	}

	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		this.protocolFactory = protocolFactory;
	}

	public void setReadFuture(ChannelReadFuture readFuture) {
		this.readFuture = readFuture;
	}

	public void upNetworkState() {

		if (next_network_weak != Long.MAX_VALUE) {

			next_network_weak = Long.MAX_VALUE;

			networkWeak = false;
		}
	}

	public void downNetworkState() {

		if (next_network_weak < Long.MAX_VALUE) {

			if (System.currentTimeMillis() > next_network_weak) {

				if (!networkWeak) {

					networkWeak = true;

					interestWrite();
				}
			}

		} else {

			next_network_weak = System.currentTimeMillis() + 64;
		}
	}

	public void wakeup() {

		upNetworkState();

		this.selectorLoop.fireEvent(this);

		this.selectionKey.interestOps(SelectionKey.OP_READ);
	}

	public int write(ByteBuffer buffer) throws IOException {
		return channel.write(buffer);
	}

	public SslReadFuture getSslReadFuture() {
		return sslReadFuture;
	}

	public void setSslReadFuture(SslReadFuture future) {
		this.sslReadFuture = future;
	}

	public boolean needFlush() {
		return writeFuture != null || writeFutures.size() > 0;
	}

	public void fireEvent(SelectorLoopEvent event) {
		this.selectorLoop.fireEvent(event);
	}


}
