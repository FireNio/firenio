package com.gifisan.nio.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ByteArrayWriteFuture;
import com.gifisan.nio.component.future.MultiWriteFuture;
import com.gifisan.nio.component.future.TextWriteFuture;
import com.gifisan.nio.component.protocol.DefaultProtocolEncoder;

public class ServerProtocolEncoder extends DefaultProtocolEncoder {

	public IOWriteFuture encode(EndPoint endPoint, Session session, String serviceName, byte[] array,
			InputStream inputStream, IOEventHandle handle) throws IOException {

		byte[] serviceNameArray = serviceName.getBytes(session.getContext().getEncoding());

		if (inputStream != null) {

			int dataLength = inputStream.available();

			ByteBuffer textBuffer = encodeAll(session.getSessionID(), serviceNameArray, array, dataLength);

			textBuffer.flip();

			if (inputStream.getClass() != ByteArrayInputStream.class) {

				return new MultiWriteFuture(endPoint, session, serviceName, textBuffer, array, inputStream, handle);
			}

			return new ByteArrayWriteFuture(endPoint, session, serviceName, textBuffer, array,
					(ByteArrayInputStream) inputStream, handle);

		}

		ByteBuffer textBuffer = encodeText(session.getSessionID(), serviceNameArray, array);

		textBuffer.flip();

		return new TextWriteFuture(endPoint, session, serviceName, textBuffer, array, handle);
	}

}
