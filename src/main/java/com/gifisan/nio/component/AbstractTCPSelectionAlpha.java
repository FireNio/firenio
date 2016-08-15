package com.gifisan.nio.component;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;

public abstract class AbstractTCPSelectionAlpha implements TCPSelectionAlpha {

	private EndPointWriter	endPointWriter;

	private ProtocolDecoder	protocolDecoder;

	private ProtocolEncoder	protocolEncoder;

	protected AbstractTCPSelectionAlpha(NIOContext context) {
		this.protocolDecoder = context.getProtocolDecoder();
		this.protocolEncoder = context.getProtocolEncoder();
	}

	public EndPointWriter getEndPointWriter() {
		return endPointWriter;
	}

	public void setEndPointWriter(EndPointWriter endPointWriter) {
		this.endPointWriter = endPointWriter;
	}

	protected TCPEndPoint attachEndPoint(NIOContext context, EndPointWriter endPointWriter, SelectionKey selectionKey)
			throws SocketException {

		TCPEndPoint endPoint = (TCPEndPoint) selectionKey.attachment();

		if (endPoint != null) {

			return endPoint;
		}

		endPoint = new DefaultTCPEndPoint(context, selectionKey, endPointWriter);

		endPoint.setProtocolDecoder(protocolDecoder);

		endPoint.setProtocolEncoder(protocolEncoder);

		selectionKey.attach(endPoint);

		return endPoint;
	}
}
