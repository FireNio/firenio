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
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueLink;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.SslReadFuture;

public class NioSocketChannel extends AbstractChannel implements com.generallycloud.nio.component.SocketChannel {

	private Socket						socket;
	private SocketChannel				channel;
	private UnsafeSession				session;
	private ChannelReadFuture			readFuture;
	private SslReadFuture				sslReadFuture;
	private SelectionKey				selectionKey;
	private boolean					networkWeak;
	private ProtocolDecoder				protocolDecoder;
	private ProtocolEncoder				protocolEncoder;
	private ProtocolFactory				protocolFactory;
	private SelectorLoop				selectorLoop;
	private ChannelWriteFuture			writeFuture;
	private boolean					opened			= true;
	private long						next_network_weak	= Long.MAX_VALUE;
	private boolean					enableInbound		= true;

	// FIXME 这里最好不要用ABQ，使用链式可增可减
	private ListQueue<ChannelWriteFuture>	writeFutures		= new ListQueueLink<ChannelWriteFuture>();

	// private ListQueue<IOWriteFuture> writeFutures = new
	// ListQueueABQ<IOWriteFuture>(1024 * 10);

	// FIXME 改进network wake 机制
	// FIXME network weak check
	public NioSocketChannel(SelectorLoop selectorLoop, SelectionKey selectionKey) throws SocketException {
		super(selectorLoop.getContext(), selectorLoop.getByteBufAllocator());
		this.selectionKey = selectionKey;
		this.selectorLoop = selectorLoop;
		this.channel = (SocketChannel) selectionKey.channel();
		this.socket = channel.socket();
		this.local = getLocalSocketAddress();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}

		this.session = context.getSessionFactory().newUnsafeSession(this);
	}

	public void close() throws IOException {
		CloseUtil.close(session);
	}

	public boolean handle(SelectorLoop selectorLoop) throws IOException {
		if (!isOpened()) {
			throw new ClosedChannelException("closed");
		}

		if (writeFuture == null) {
			writeFuture = writeFutures.poll();
		}

		if (writeFuture == null) {
			return true;
		}

		if (!writeFuture.write(this)) {
			return isNetworkWeak();
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

	public UnsafeSession getSession() {
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

		ReleaseUtil.release(writeFuture);

		ListQueue<ChannelWriteFuture> writeFutures = this.writeFutures;

		if (writeFutures.size() != -1) {
			return;
		}

		ChannelWriteFuture f = writeFutures.poll();

		UnsafeSession session = this.session;

		ClosedChannelException e = new ClosedChannelException(session.toString());

		for (; f != null;) {

			f.onException(session, e);

			ReleaseUtil.release(f);

			f = writeFutures.poll();
		}
	}

	// FIXME 这里有问题
	public void physicalClose() throws IOException {

		enableInbound = false;

		int tryTime = 5;

		if (channel.isOpen()) {
			tryTime <<= 3;
		}

		// FIXME condition instead?
		for (;;) {
			if (!needFlush() || tryTime-- == 0) {
				break;
			}
			ThreadUtil.sleep(6);
		}

		this.opened = false;

		this.releaseWriteFutures();

		this.selectionKey.attach(null);

		try {
			this.channel.close();
		} catch (Exception e) {
		}

		this.selectionKey.cancel();

		ReleaseUtil.release(readFuture);
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

	public void wakeup() throws IOException {

		upNetworkState();
		
		ReentrantLock lock = channelLock;

		lock.lock();

		try {

			selectorLoop.fireEvent(this);

		} finally {

			lock.unlock();
		}

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

}
