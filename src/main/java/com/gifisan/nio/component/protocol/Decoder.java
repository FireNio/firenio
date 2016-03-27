package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.ProtocolData;

public interface Decoder {
	
	public abstract void decode(EndPoint endPoint, ProtocolData data, byte[] header,ByteBuffer buffer) throws IOException;
	
	public abstract boolean progressRead(EndPoint endPoint ,ByteBuffer buffer) throws IOException;
}
