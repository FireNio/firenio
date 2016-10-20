package com.generallycloud.nio.component;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public abstract class AbstractTCPSelectionAlpha implements SocketChannelSelectionAlpha {

	private ChannelFlusher	channelFlusher;
	
	private ProtocolFactory protocolFactory;
	
	private ProtocolDecoder protocolDecoder;
	
	private ProtocolEncoder protocolEncoder;
	
	protected AbstractTCPSelectionAlpha(NIOContext context) {
		this.protocolFactory = context.getProtocolFactory();
		this.protocolDecoder = protocolFactory.getProtocolDecoder();
		this.protocolEncoder = protocolFactory.getProtocolEncoder();
	}

	public ChannelFlusher getChannelFlusher() {
		return channelFlusher;
	}

	public void setChannelFlusher(ChannelFlusher channelFlusher) {
		this.channelFlusher = channelFlusher;
	}

	protected SocketChannel attachSocketChannel(NIOContext context, ChannelFlusher channelFlusher, SelectionKey selectionKey)
			throws SocketException {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel != null) {

			return channel;
		}

		channel = new NioSocketChannel(context, selectionKey, channelFlusher);

		channel.setProtocolDecoder(protocolDecoder);

		channel.setProtocolEncoder(protocolEncoder);
		
		channel.setProtocolFactory(protocolFactory);
		
		selectionKey.attach(channel);

		return channel;
	}
}
