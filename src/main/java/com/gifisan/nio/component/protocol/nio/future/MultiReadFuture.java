package com.gifisan.nio.component.protocol.nio.future;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.future.IOReadFuture;

public class MultiReadFuture extends AbstractNIOReadFuture implements IOReadFuture {

	private int				dataLength;
	private ByteBuffer			streamBuffer;
	private int				readLength	= -1;
	private static final Logger	logger		= LoggerFactory.getLogger(MultiReadFuture.class);

	public MultiReadFuture(Session session, ByteBuffer header) {
		super(session, header);
	}

	protected void decode(TCPEndPoint endPoint, byte[] header) {

		this.hasStream = true;

		this.dataLength = gainStreamLength(header);

		int bufferLength = 1024 * 256;

		bufferLength = dataLength > bufferLength ? bufferLength : dataLength;

		this.streamBuffer = ByteBuffer.allocate(bufferLength);
	}

	protected boolean doRead(TCPEndPoint endPoint) throws IOException {

		if (readLength == -1) {

			IOEventHandleAdaptor eventHandle = session.getContext().getIOEventHandleAdaptor();

			try {
				eventHandle.acceptAlong(session, this);
			} catch (Exception e) {
				logger.debug(e);
			}

			if (this.outputStream == null) {
				throw new IOException("none outputstream");
			}
			readLength = 0;
		}

		if (readLength < dataLength) {
			ByteBuffer buffer = streamBuffer;

			endPoint.read(buffer);

			fill(outputStream, buffer);
		}

		return readLength == dataLength;
	}

	private void fill(OutputStream outputStream, ByteBuffer buffer) throws IOException {

		byte[] array = buffer.array();

		int length = buffer.position();

		if (length == 0) {
			return;
		}

		readLength += length;

		outputStream.write(array, 0, buffer.position());

		buffer.clear();
	}

	public int getStreamLength() {
		return dataLength;
	}

}
