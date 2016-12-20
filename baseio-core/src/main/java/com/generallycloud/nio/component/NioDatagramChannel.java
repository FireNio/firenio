/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
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

	@Override
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

			@Override
			public boolean handle(SelectorLoop selectLoop) throws IOException {

				CloseUtil.close(NioDatagramChannel.this);

				return false;
			}
		});
	}
	
	public void fireEvent(SelectorLoopEvent event) {
		this.selectorLoop.fireEvent(event);
	}
	
	@Override
	public DatagramChannelContext getContext() {
		return context;
	}

	@Override
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

	@Override
	public InetSocketAddress getLocalSocketAddress() {
		if (local == null) {
			local = (InetSocketAddress) socket.getLocalSocketAddress();
		}
		return local;
	}

	@Override
	protected String getMarkPrefix() {
		return "UDP";
	}

	@Override
	public int getMaxIdleTime() throws SocketException {
		return socket.getSoTimeout();
	}

	@Override
	public InetSocketAddress getRemoteSocketAddress() {
		return remote;
	}

	@Override
	public UnsafeDatagramSession getSession() {
		return session;
	}

	private void sendPacket(ByteBuf buf, SocketAddress socketAddress) throws IOException {
		channel.send(buf.nioBuffer(), socketAddress);
	}

	@Override
	public boolean isOpened() {
		return channel.isConnected() || channel.isOpen();
	}

	@Override
	public void sendPacket(DatagramPacket packet, SocketAddress socketAddress) throws IOException {
		ByteBuf buf = allocate(packet);
		try{
			sendPacket(buf.flip(), socketAddress);
		}finally{
			ReleaseUtil.release(buf);
		}
	}

	@Override
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
