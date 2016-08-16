package com.gifisan.nio.component.protocol;

import java.io.IOException;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.future.IOWriteFuture;
import com.gifisan.nio.component.protocol.future.ReadFuture;

public interface ProtocolEncoder {

	public abstract IOWriteFuture encode(TCPEndPoint endPoint,ReadFuture readFuture) throws IOException;

}
