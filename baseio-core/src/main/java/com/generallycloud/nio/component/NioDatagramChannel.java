package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicBoolean;

import com.generallycloud.nio.acceptor.DatagramChannelFactory;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class NioDatagramChannel extends AbstractChannel implements com.generallycloud.nio.component.DatagramChannel {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(NioDatagramChannel.class);
	private AtomicBoolean		_closed	= new AtomicBoolean(false);
	private DatagramChannel		channel;
	private IOSession			session;
	private DatagramSocket		socket;

	public NioDatagramChannel(NIOContext context, SelectionKey selectionKey, InetSocketAddress remote) throws SocketException{
		this(context,(DatagramChannel) selectionKey.channel(),remote);
	}
	
	public NioDatagramChannel(NIOContext context, DatagramChannel channel, InetSocketAddress remote)
			throws SocketException {
		super(context);
		this.channel = channel;
		this.remote = remote;
		this.socket = channel.socket();
		if (socket == null) {
			throw new SocketException("null socket");
		}
	}

	public void close() throws IOException {
		this.physicalClose();
	}

	public void physicalClose() throws IOException {
		
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

	public IOSession getSession() {
		return session;
	}

	public void sendPacket(ByteBuffer buffer) throws IOException {

		channel.send(buffer, getRemoteSocketAddress());
	}

	public void sendPacket(ByteBuffer buffer, SocketAddress socketAddress) throws IOException {

		channel.send(buffer, socketAddress);
	}

	public void setSession(Session session) {
		this.session = (IOSession) session;
	}

	public boolean isOpened() {
		return channel.isConnected() || channel.isOpen();
	}

}
