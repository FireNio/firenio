package com.gifisan.nio.component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.protocol.udp.DatagramPacket;
import com.gifisan.nio.server.NIOContext;

public class DefaultUDPEndPoint extends AbstractEndPoint implements UDPEndPoint {

	private DatagramChannel	channel		= null;
	
	private Session		session		= null;
	private DatagramSocket	socket		= null;
	private AtomicBoolean	_closed		= new AtomicBoolean(false);
	private AtomicInteger	writers		= new AtomicInteger();

	public DefaultUDPEndPoint(NIOContext context,DatagramChannel channel) throws SocketException {
		super(context);
		this.channel = channel;
		this.socket = channel.socket();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}
	}

	public void close() throws IOException {

		if (writers.get() > 0) {
			return;
		}

		if (_closed.compareAndSet(false, true)) {

			this.endConnect = true;

			DebugUtil.debug(">>>> rm " + this.toString());

			session.destroyImmediately();

			this.channel.close();

		}
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public int read(ByteBuffer buffer) throws IOException {
		return this.channel.read(buffer);
	}

	public int write(ByteBuffer buffer) throws IOException {
		return channel.write(buffer);
	}

	public void incrementWriter() {
		writers.incrementAndGet();
	}

	public void decrementWriter() {
		writers.decrementAndGet();
	}

	public DatagramPacket readPacket(ByteBuffer buffer) throws IOException {

		SocketAddress socketAddress = channel.receive(buffer);
		
		DebugUtil.debug("========"+socketAddress);
		
//		int length = this.read(buffer);

		DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.position());

		buffer.clear();

		return packet;
	}

	public Session getTCPSession() {
		return session;
	}

	public void setTCPSession(Session session) {
		this.session = session;
	}
	
	

}
