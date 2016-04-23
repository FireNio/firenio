package com.gifisan.nio.component;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.client.IOWriteFuture;

public interface ProtocolEncoder {

	public abstract IOWriteFuture encode(Session session, String serviceName, byte[] array, InputStream inputStream,
			IOExceptionHandle handle) throws IOException;

}
