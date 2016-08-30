package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.component.TCPEndPoint;

public interface ProtocolDecoder {

	public abstract IOReadFuture decode(TCPEndPoint endPoint) throws IOException;

}