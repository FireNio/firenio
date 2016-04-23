package com.gifisan.nio.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.client.IOWriteFuture;
import com.gifisan.nio.component.AbstractProtocolEncoder;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.ByteArrayWriteFuture;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.MultiWriteFuture;
import com.gifisan.nio.component.ProtocolEncoder;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TextWriteFuture;

public class ServerProtocolEncoder extends AbstractProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(Session session,String serviceName, byte[] array, InputStream inputStream, IOExceptionHandle handle)
			throws IOException {

		byte [] serviceNameArray = serviceName.getBytes(session.getContext().getEncoding());
		
		if (inputStream != null) {

			int dataLength = inputStream.available();

			ByteBuffer textBuffer = encodeAll(session.getSessionID(),serviceNameArray, array, dataLength);

			textBuffer.flip();

			if (inputStream.getClass() != ByteArrayInputStream.class) {

				return new MultiWriteFuture(session,serviceName, textBuffer, array, inputStream, handle);
			}

			return new ByteArrayWriteFuture(session,serviceName, textBuffer, array, (ByteArrayInputStream) inputStream, handle);

		}

		ByteBuffer textBuffer = encodeText(session.getSessionID(),serviceNameArray, array);

		textBuffer.flip();

		return new TextWriteFuture(session,serviceName, textBuffer, array, handle);
	}

}
