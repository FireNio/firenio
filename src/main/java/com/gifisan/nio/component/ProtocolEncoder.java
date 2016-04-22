package com.gifisan.nio.component;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.WriteFuture;

public interface ProtocolEncoder {

	public abstract WriteFuture encode(Session session, byte[] array, InputStream inputStream,
			IOExceptionHandle handle) throws IOException;

}
