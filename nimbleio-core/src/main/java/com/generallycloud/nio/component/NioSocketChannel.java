package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.ChannelWriterImpl.ChannelWriteEvent;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.IOWriteFuture;
import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ProtocolFactory;

public class NioSocketChannel extends AbstractChannel implements com.generallycloud.nio.component.SocketChannel {

	private boolean			networkWeak;
	private SocketChannel		channel;
	private IOWriteFuture		currentWriteFuture;
	private boolean			opened			= true;
	private ChannelWriter		channelWriter;
	private IOReadFuture		readFuture;
	private SelectionKey		selectionKey;
	private IOSession			session;
	private Socket				socket;
	private long				next_network_weak	= Long.MAX_VALUE;
	private ProtocolEncoder		protocolEncoder;
	private ProtocolDecoder		protocolDecoder;
	private ProtocolFactory		protocolFactory;
	
	// FIXME 改进network wake 机制
	// FIXME network weak check
	public NioSocketChannel(NIOContext context, SelectionKey selectionKey, ChannelWriter channelWriter)
			throws SocketException {
		super(context);
		this.selectionKey = selectionKey;
		this.channelWriter = channelWriter;
		this.channel = (SocketChannel) selectionKey.channel();
		this.socket = channel.socket();
		this.local = getLocalSocketAddress();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}
		
		this.session = new IOSessionImpl(this, getChannelID());
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		this.protocolEncoder = protocolEncoder;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		this.protocolDecoder = protocolDecoder;
	}
	
	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		this.protocolFactory = protocolFactory;
	}

	public void updateNetworkState(int length) {

		if (length == 0) {
			if (next_network_weak < Long.MAX_VALUE) {
				
				if (System.currentTimeMillis() > next_network_weak) {
					
					if (!networkWeak) {

						networkWeak = true;
						
						interestWrite();
					}
				}
			}else{
				
				next_network_weak = System.currentTimeMillis() + 64;
			}
		}else{
			
			if (next_network_weak != Long.MAX_VALUE) {
				
				next_network_weak = Long.MAX_VALUE;
				
				networkWeak = false;
			}
		}
	}

	public void close() throws IOException {
		CloseUtil.close(session);
	}
	
	public void physicalClose() throws IOException {
		
		this.opened = false;

		this.selectionKey.attach(null);

		this.channel.close();
	}

	public void wakeup() throws IOException {

		this.channelWriter.fire(new ChannelWriteEvent() {
			
			public void handle(ChannelWriter channelWriter) {
				
				NioSocketChannel channel = NioSocketChannel.this;
				
				channel.updateNetworkState(1);
				
				channelWriter.wekeupSocketChannel(channel);
			}
		});

		this.selectionKey.interestOps(SelectionKey.OP_READ);
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

	public IOReadFuture getReadFuture() {
		return readFuture;
	}

	public InetSocketAddress getRemoteSocketAddress() {
		if (remote == null) {
			remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		}
		return remote;
	}

	public IOSession getSession() {
		return session;
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

	//FIXME 是否使用channel.isOpen()
	public boolean isOpened() {
		return opened;
	}

	public int read(ByteBuffer buffer) throws IOException {
		return this.channel.read(buffer);
	}

	public void setCurrentWriteFuture(IOWriteFuture future) {
		this.currentWriteFuture = future;
	}

	public IOWriteFuture getCurrentWriteFuture() {
		return currentWriteFuture;
	}

	public void setReadFuture(IOReadFuture readFuture) {
		this.readFuture = readFuture;
	}

	public int write(ByteBuffer buffer) throws IOException {
		return channel.write(buffer);
	}

	public void offer(IOWriteFuture future) {
		this.channelWriter.offer(future);
	}
}
