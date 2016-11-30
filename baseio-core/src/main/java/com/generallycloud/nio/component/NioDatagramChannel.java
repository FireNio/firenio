package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;
import com.generallycloud.nio.connector.ChannelConnector;
import com.generallycloud.nio.protocol.DatagramPacket;


public class NioDatagramChannel extends AbstractChannel implements com.generallycloud.nio.component.DatagramChannel {

	private static final Logger logger = LoggerFactory.getLogger(NioDatagramChannel.class);
	
	private DatagramChannel			channel;
	private DatagramSocket			socket;
	private DatagramChannelContext	context;
	private UnsafeDatagramSession		session;

	public NioDatagramChannel(DatagramChannelSelectorLoop selectorLoop, DatagramChannel channel, InetSocketAddress remote)
			throws IOException {
		super(selectorLoop);
		this.context = selectorLoop.getContext();
		this.channel = channel;
		this.remote = remote;
		this.socket = channel.socket();
		if (socket == null) {
			throw new SocketException("null socket");
		}

		session = new UnsafeDatagramSessionImpl(this, context.getSequence().AUTO_CHANNEL_ID.getAndIncrement());
	
		session.fireOpend();
		
	}

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
	
	private void fireClose() {

		fireEvent(new SelectorLoopEventAdapter() {

			public boolean handle(SelectorLoop selectLoop) throws IOException {

				CloseUtil.close(NioDatagramChannel.this);

				return false;
			}
		});
	}
	
	public void fireEvent(SelectorLoopEvent event) {
		this.selectorLoop.fireEvent(event);
	}
	
	public DatagramChannelContext getContext() {
		return context;
	}

	public void physicalClose() {

		DatagramSessionManager manager = context.getSessionManager();
		
		manager.removeSession(session);
		
		ChannelService service = context.getChannelService();

		if (service instanceof ChannelConnector) {

			try {
				((ChannelConnector) service).physicalClose();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
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
