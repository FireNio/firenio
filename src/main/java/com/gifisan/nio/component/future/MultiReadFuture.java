package com.gifisan.nio.component.future;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.OutputStreamAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;

public class MultiReadFuture extends AbstractReadFuture implements IOReadFuture {

	private int				dataLength	= 0;
	private int				readLength	= -1;
	private ByteBuffer			streamBuffer	= null;
	private static final Logger	logger		= LoggerFactory.getLogger(MultiReadFuture.class);

	public MultiReadFuture(TCPEndPoint endPoint, Session session) {
		super(endPoint, session);
	}

	protected void decode(TCPEndPoint endPoint, byte[] header) throws IOException {

		this.hasStream = true;

		this.dataLength = gainStreamLength(header);

		int bufferLength = 1024 * 1000;

		bufferLength = dataLength > bufferLength ? bufferLength : dataLength;

		this.streamBuffer = ByteBuffer.allocate(bufferLength);
	}

	protected boolean doRead(TCPEndPoint endPoint) throws IOException {

		if (readLength == -1) {
			AbstractSession _Session = (AbstractSession) this.session;

			OutputStreamAcceptor outputStreamAcceptor = _Session.getOutputStreamAcceptor();

			try {
				outputStreamAcceptor.accept(_Session, this);
			} catch (Exception e) {
				logger.debug(e);
			}

			if (!this.hasOutputStream()) {
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
