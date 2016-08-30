package com.gifisan.nio.component.protocol;

import java.io.IOException;

import com.gifisan.nio.component.TCPEndPoint;

public interface ProtocolDecoder {

	public abstract IOReadFuture decode(TCPEndPoint endPoint) throws IOException;

}