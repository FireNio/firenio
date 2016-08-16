package com.gifisan.nio.component.protocol;

import java.io.IOException;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.future.IOReadFuture;

public interface ProtocolDecoder {

	public abstract IOReadFuture decode(TCPEndPoint endPoint) throws IOException;

}