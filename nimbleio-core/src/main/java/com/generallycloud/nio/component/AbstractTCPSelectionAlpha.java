package com.generallycloud.nio.component;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ProtocolFactory;

public abstract class AbstractTCPSelectionAlpha implements SocketChannelSelectionAlpha {

	private ChannelWriter	channelWriter;
	
	private ProtocolFactory protocolFactory;
	
	private ProtocolDecoder protocolDecoder;
	
	private ProtocolEncoder protocolEncoder;
	
	protected AbstractTCPSelectionAlpha(NIOContext context) {
		this.protocolFactory = context.getProtocolFactory();
		this.protocolDecoder = protocolFactory.getProtocolDecoder();
		this.protocolEncoder = protocolFactory.getProtocolEncoder();
	}

	public ChannelWriter getChannelWriter() {
		return channelWriter;
	}

	public void setChannelWriter(ChannelWriter channelWriter) {
		this.channelWriter = channelWriter;
	}

	protected SocketChannel attachSocketChannel(NIOContext context, ChannelWriter channelWriter, SelectionKey selectionKey)
			throws SocketException {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel != null) {

			return channel;
		}

		channel = new NioSocketChannel(context, selectionKey, channelWriter);

		channel.setProtocolDecoder(protocolDecoder);

		channel.setProtocolEncoder(protocolEncoder);
		
		channel.setProtocolFactory(protocolFactory);
		
		selectionKey.attach(channel);

		return channel;
	}
}
