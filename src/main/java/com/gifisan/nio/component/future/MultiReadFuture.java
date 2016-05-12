package com.gifisan.nio.component.future;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.OutputStreamAcceptor;
import com.gifisan.nio.component.Session;

public class MultiReadFuture extends AbstractReadFuture implements IOReadFuture {

	public MultiReadFuture(TCPEndPoint endPoint,ByteBuffer textBuffer, Session session, String serviceName,int dataLength) {
		super(endPoint,textBuffer, session, serviceName);
		this.hasStream = true;
		this.dataLength = dataLength;
		int bufferLength = 1024 * 1000;
		bufferLength = dataLength > bufferLength ? bufferLength : dataLength;
		this.streamBuffer = ByteBuffer.allocate(bufferLength);
//		this.streamBuffer.position(streamBuffer.limit());
	}

	private int				readLength	= 0;
	private int				dataLength	= 0;
	private ByteBuffer			streamBuffer	= null;

	public boolean read() throws IOException {
		
		ByteBuffer buffer = this.textBuffer;

		if (buffer.hasRemaining()) {
			endPoint.read(buffer);
			if (buffer.hasRemaining()) {
				return false;
			}
			
			AbstractSession _Session = (AbstractSession) this.session;
			
			OutputStreamAcceptor outputStreamAcceptor = _Session.getOutputStreamAcceptor();
			
			try {
				outputStreamAcceptor.accept(_Session, this);
			} catch (Exception e) {
				DebugUtil.debug(e);
			}
			
			if (!this.hasOutputStream()) {
				throw new IOException("none outputstream");
			}
		}

		if (readLength < dataLength) {
			buffer = streamBuffer;

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
	
	public int getStreamLength(){
		return dataLength;
	}
}
