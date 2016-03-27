package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.protocol.Decoder;

public class SlowlyNetworkReader implements EndPointSchedule{

	public SlowlyNetworkReader(Decoder decoder, ProtocolData protocolData, ByteBuffer buffer) {
		this.decoder = decoder;
		this.protocolData = protocolData;
		this.buffer = buffer;
	}

	private Decoder			decoder		= null;
	private ProtocolData	protocolData	= null;
	private ByteBuffer			buffer		= null;

	protected Decoder getDecoder() {
		return decoder;
	}

	public ProtocolData getProtocolData() {
		return protocolData;
	}

	protected ByteBuffer getBuffer() {
		return buffer;
	}

	public boolean schedule(EndPoint endPoint) throws IOException {

		if (decoder.progressRead(endPoint, buffer)) {

			decoder.decode(endPoint, protocolData, protocolData.getHeader(), buffer);

			endPoint.setSchedule(null);

			return true;
		}
		return false;
	}

}
