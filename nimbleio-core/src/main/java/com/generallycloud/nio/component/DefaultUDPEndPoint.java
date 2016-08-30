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

import com.generallycloud.nio.acceptor.UDPEndPointFactory;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class DefaultUDPEndPoint extends AbstractEndPoint implements UDPEndPoint {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(DefaultUDPEndPoint.class);
	private AtomicBoolean		_closed	= new AtomicBoolean(false);
	private DatagramChannel		channel;
	private Session			session;
	private DatagramSocket		socket;

	public DefaultUDPEndPoint(NIOContext context, SelectionKey selectionKey, InetSocketAddress remote) throws SocketException{
		this(context,(DatagramChannel) selectionKey.channel(),remote);
	}
	
	public DefaultUDPEndPoint(NIOContext context, DatagramChannel channel, InetSocketAddress remote)
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
		throw new UnsupportedOperationException("physicalClose close instead");
	}

	public void physicalClose() throws IOException {
		
		if (_closed.compareAndSet(false, true)) {

			UDPEndPointFactory factory = getContext().getUDPEndPointFactory();

			factory.removeUDPEndPoint(this);
			
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

	public Session getSession() {
		return session;
	}

	public void sendPacket(ByteBuffer buffer) throws IOException {

		channel.send(buffer, getRemoteSocketAddress());
	}

	public void sendPacket(ByteBuffer buffer, SocketAddress socketAddress) throws IOException {

		channel.send(buffer, socketAddress);
	}

	public void setSession(Session session) {
		this.session = (Session) session;
	}

}
