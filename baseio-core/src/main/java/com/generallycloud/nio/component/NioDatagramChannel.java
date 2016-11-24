package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import com.generallycloud.nio.acceptor.DatagramChannelFactory;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class NioDatagramChannel extends AbstractChannel implements com.generallycloud.nio.component.DatagramChannel {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(NioDatagramChannel.class);
	private AtomicBoolean		_closed	= new AtomicBoolean(false);
	private DatagramChannel		channel;
	private DatagramSocket		socket;
	private UnsafeDatagramSession	session; //FIXME new 

	public NioDatagramChannel(SelectorLoop selectorLoop, DatagramChannel channel, InetSocketAddress remote)
			throws SocketException {
		super(selectorLoop.getContext(), selectorLoop.getByteBufAllocator());
		this.channel = channel;
		this.remote = remote;
		this.socket = channel.socket();
		if (socket == null) {
			throw new SocketException("null socket");
		}
		
		session = new UnsafeDatagramSessionImpl(this, context.getSequence().AUTO_CHANNEL_ID.getAndIncrement());
	}

	public void close() throws IOException {
		this.physicalClose();
	}

	public void physicalClose() {

		if (_closed.compareAndSet(false, true)) {

			DatagramChannelFactory factory = getContext().getDatagramChannelFactory();

			factory.removeDatagramChannel(this);

			LOGGER.debug(">>>> rm {}", this.toString());
		}

	}

	public InetSocketAddress getLocalSocketAddress() {
		if (local == null) {
			local = (InetSocketAddress) socket.getLocalSocketAddress();
		}
		return local;
	}

	protected String getMarkPrefix() {
		return "UDP";
	}

	public int getMaxIdleTime() throws SocketException {
		return socket.getSoTimeout();
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return remote;
	}

	public UnsafeDatagramSession getSession() {
		return session;
	}

	public void sendPacket(ByteBuf buf) throws IOException {

		channel.send(buf.nioBuffer(), getRemoteSocketAddress());
	}

	public void sendPacket(ByteBuf buf, SocketAddress socketAddress) throws IOException {

		channel.send(buf.nioBuffer(), socketAddress);
	}

	public boolean isOpened() {
		return channel.isConnected() || channel.isOpen();
	}

}
