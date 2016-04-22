package com.gifisan.nio.component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.ByteArrayWriteFuture;
import com.gifisan.nio.service.MultiWriteFuture;
import com.gifisan.nio.service.TextWriteFuture;
import com.gifisan.nio.service.WriteFuture;

public class ServerProtocolEncoder extends AbstractProtocolEncoder implements ProtocolEncoder {

	public WriteFuture encode(Session session, byte[] array, InputStream inputStream, IOExceptionHandle handle)
			throws IOException {

		if (inputStream != null) {

			int dataLength = inputStream.available();

			ByteBuffer textByteBuffer = encodeAll(session.getSessionID(), array, dataLength);

			textByteBuffer.flip();

			if (inputStream.getClass() != ByteArrayInputStream.class) {

				return new MultiWriteFuture(handle, textByteBuffer, session, inputStream);
			}

			return new ByteArrayWriteFuture(handle, textByteBuffer, session, (ByteArrayInputStream) inputStream);

		}

		ByteBuffer textByteBuffer = encodeText(session.getSessionID(), array);

		textByteBuffer.flip();

		return new TextWriteFuture(handle, textByteBuffer, session);
	}

}
