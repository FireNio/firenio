package com.gifisan.nio.component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.protocol.udp.DatagramPacket;
import com.gifisan.nio.server.NIOContext;

//FIXME 不可以根据selection key 来
public abstract class AbstractUDPEndPoint extends AbstractEndPoint implements UDPEndPoint {

	private DatagramChannel	channel		= null;
	private DatagramSocket	socket		= null;
	private AtomicBoolean	_closed		= new AtomicBoolean(false);
	
	

	public AbstractUDPEndPoint(NIOContext context,DatagramChannel channel) throws SocketException {
		super(context);
		this.channel = channel;
		this.socket = channel.socket();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}
	}

	public void close() throws IOException {

		if (_closed.compareAndSet(false, true)) {

			DebugUtil.debug(">>>> rm " + this.toString());
			
			extendClose();
			
			this.channel.close();
			
		}
	}
	
	protected abstract void extendClose();

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public int read(ByteBuffer buffer) throws IOException {
		return this.channel.read(buffer);
	}

	public int write(ByteBuffer buffer) throws IOException {
		return channel.write(buffer);
	}

	public DatagramPacket readPacket(ByteBuffer buffer) throws IOException {

//		SocketAddress address = channel.receive(buffer);
//		
//		if (remote == null) {
//			remote = (InetSocketAddress)address;
//		}
		
		this.remote = (InetSocketAddress) channel.receive(buffer);
		
		return new DatagramPacket(buffer);
	}
	
	public void sendPacket(ByteBuffer buffer,SocketAddress socketAddress) throws IOException {
		
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
			local = (InetSocketAddress)socket.getLocalSocketAddress();
		}
		return local;
	}

	protected InetSocketAddress getRemoteSocketAddress() {
		if (remote == null) {
			remote = (InetSocketAddress)socket.getRemoteSocketAddress();
		}
		return remote;
	}

	protected String getMarkPrefix() {
		return "UDP";
	}
	
	
}
