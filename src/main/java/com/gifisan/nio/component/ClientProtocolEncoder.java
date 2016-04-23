package com.gifisan.nio.component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.client.IOWriteFuture;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.ByteArrayWriteFuture;
import com.gifisan.nio.service.MultiWriteFuture;
import com.gifisan.nio.service.TextWriteFuture;

public class ClientProtocolEncoder extends AbstractProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(Session session, byte[] array, InputStream inputStream, IOExceptionHandle handle)
			throws IOException {

		if (inputStream != null) {

			int dataLength = inputStream.available();

			ByteBuffer textBuffer = encodeAll(session.getSessionID(), array, dataLength);

			textBuffer.flip();

			if (inputStream.getClass() != ByteArrayInputStream.class) {

				return new MultiWriteFuture(session, textBuffer, array, inputStream, handle);
			}

			return new ByteArrayWriteFuture(session, textBuffer, array, (ByteArrayInputStream) inputStream, handle);

		}

		ByteBuffer textBuffer = encodeText(session.getSessionID(), array);

		textBuffer.flip();

		return new TextWriteFuture(session, textBuffer, array, handle);
	}

}
