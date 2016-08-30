package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.component.TCPEndPoint;

public interface ProtocolEncoder {

	public abstract IOWriteFuture encode(TCPEndPoint endPoint, IOReadFuture future) throws IOException;

}
