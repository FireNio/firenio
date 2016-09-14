package com.generallycloud.nio.component;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ProtocolFactory;

public abstract class AbstractTCPSelectionAlpha implements TCPSelectionAlpha {

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

	protected TCPEndPoint attachEndPoint(NIOContext context, ChannelWriter channelWriter, SelectionKey selectionKey)
			throws SocketException {

		TCPEndPoint endPoint = (TCPEndPoint) selectionKey.attachment();

		if (endPoint != null) {

			return endPoint;
		}

		endPoint = new DefaultTCPEndPoint(context, selectionKey, channelWriter);

		endPoint.setProtocolDecoder(protocolDecoder);

		endPoint.setProtocolEncoder(protocolEncoder);
		
		endPoint.setProtocolFactory(protocolFactory);
		
		selectionKey.attach(endPoint);

		return endPoint;
	}
}
