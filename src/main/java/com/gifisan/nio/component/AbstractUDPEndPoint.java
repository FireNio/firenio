package com.gifisan.nio.component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;

//FIXME 不可以根据selection key 来
public abstract class AbstractUDPEndPoint extends AbstractEndPoint implements UDPEndPoint {

	private DatagramChannel	channel	;
	private DatagramSocket	socket	;
	private AtomicBoolean	_closed	= new AtomicBoolean(false);
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUDPEndPoint.class);

	public AbstractUDPEndPoint(NIOContext context, DatagramChannel channel, InetSocketAddress remote)
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

		if (_closed.compareAndSet(false, true)) {

			UDPEndPointFactory factory = getContext().getUDPEndPointFactory();
			
			factory.removeUDPEndPoint(this);
			
			LOGGER.debug(">>>> rm {}" , this.toString());

		}
	}

	public void sendPacket(ByteBuffer buffer, SocketAddress socketAddress) throws IOException {

		channel.send(buffer, socketAddress);
	}

	public void sendPacket(ByteBuffer buffer) throws IOException {

		channel.send(buffer, getRemoteSocketAddress());
	}

	public int getMaxIdleTime() throws SocketException {
		return socket.getSoTimeout();
	}

	protected InetSocketAddress getLocalSocketAddress() {
		if (local == null) {
			local = (InetSocketAddress) socket.getLocalSocketAddress();
		}
		return local;
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return remote;
	}

	protected String getMarkPrefix() {
		return "UDP";
	}

}
