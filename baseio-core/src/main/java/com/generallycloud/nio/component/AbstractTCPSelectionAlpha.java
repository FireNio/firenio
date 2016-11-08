package com.generallycloud.nio.component;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public abstract class AbstractTCPSelectionAlpha implements SocketChannelSelectionAlpha {

	protected SelectorLoop		selectorLoop;

	protected ProtocolFactory	protocolFactory;

	protected ProtocolDecoder	protocolDecoder;

	protected ProtocolEncoder	protocolEncoder;

	protected AbstractTCPSelectionAlpha(BaseContext context,SelectorLoop selectorLoop) {
		this.selectorLoop = selectorLoop;
		this.protocolFactory = context.getProtocolFactory();
		this.protocolDecoder = protocolFactory.getProtocolDecoder();
		this.protocolEncoder = protocolFactory.getProtocolEncoder();
	}

	protected SocketChannel attachSocketChannel(SelectionKey selectionKey) throws SocketException {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel != null) {

			return channel;
		}
		
		BaseContext context = selectorLoop.getContext();
		
		ChannelFlusher channelFlusher = selectorLoop.getChannelFlusher();

		channel = new NioSocketChannel(context, selectionKey, channelFlusher);

		channel.setProtocolDecoder(protocolDecoder);

		channel.setProtocolEncoder(protocolEncoder);

		channel.setProtocolFactory(protocolFactory);

		selectionKey.attach(channel);

		return channel;
	}

}
