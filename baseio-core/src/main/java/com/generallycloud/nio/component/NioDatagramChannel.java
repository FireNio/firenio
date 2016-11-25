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
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.protocol.DatagramPacket;

public class NioDatagramChannel extends AbstractChannel implements com.generallycloud.nio.component.DatagramChannel {

	private static final Logger		LOGGER	= LoggerFactory.getLogger(NioDatagramChannel.class);
	private AtomicBoolean			_closed	= new AtomicBoolean(false);
	private DatagramChannel			channel;
	private DatagramSocket			socket;
	private DatagramChannelContext	context;
	private UnsafeDatagramSession		session;

	public NioDatagramChannel(DatagramChannelSelectorLoop selectorLoop, DatagramChannel channel, InetSocketAddress remote)
			throws SocketException {
		super(selectorLoop.getContext(), selectorLoop.getByteBufAllocator());
		this.context = selectorLoop.getContext();
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
	
	public DatagramChannelContext getContext() {
		return context;
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

	private void sendPacket(ByteBuf buf, SocketAddress socketAddress) throws IOException {
		channel.send(buf.nioBuffer(), socketAddress);
	}

	public boolean isOpened() {
		return channel.isConnected() || channel.isOpen();
	}

	public void sendPacket(DatagramPacket packet, SocketAddress socketAddress) throws IOException {
		ByteBuf buf = allocate(packet);
		try{
			sendPacket(buf.flip(), socketAddress);
		}finally{
			ReleaseUtil.release(buf);
		}
	}

	public void sendPacket(DatagramPacket packet) throws IOException {
		sendPacket(packet, remote);
	}
	
	private ByteBuf allocate(DatagramPacket packet) {
		
		if (packet.getTimestamp() == -1) {
			
			int length = packet.getData().length;
			
			ByteBuf buf = session.getByteBufAllocator().allocate(DatagramPacket.PACKET_HEADER + length);
			buf.skipBytes(DatagramPacket.PACKET_HEADER);
			buf.put(packet.getData());
			return buf;
		}

		return allocate(packet.getTimestamp(), packet.getSequenceNo(), packet.getData());
	}

	private ByteBuf allocate(long timestamp, int sequenceNO, byte[] data) {

		ByteBuf buf = session.getByteBufAllocator().allocate(DatagramPacket.PACKET_MAX);
		
		buf.putLong(0);
		buf.putInt(sequenceNO);
		buf.put(data);
		
		return buf;
	}

}
