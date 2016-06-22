package com.gifisan.nio.component;

import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;

public class AbstractIOService {
	
	private IOEventHandle ioEventHandle = null;

	private ProtocolDecoder protocolDecoder = null;

	private ProtocolEncoder protocolEncoder = null;

	public IOEventHandle getIoEventHandle() {
		return ioEventHandle;
	}
	
	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public void setIoEventHandle(IOEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}

	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		this.protocolDecoder = protocolDecoder;
	}

	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		this.protocolEncoder = protocolEncoder;
	}
	
}
