package com.gifisan.nio.component.future;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.OutputStreamAcceptor;
import com.gifisan.nio.component.Session;

public class StreamReadFuture extends AbstractReadFuture implements IOReadFuture {

	public StreamReadFuture(EndPoint endPoint,Session session, String serviceName,int dataLength) {
		super(endPoint,null, session, serviceName);
		this.hasStream = true;
		this.dataLength = dataLength;
		int bufferLength = 1024 * 1000;
		bufferLength = dataLength > bufferLength ? bufferLength : dataLength;
		this.streamBuffer = ByteBuffer.allocate(bufferLength);
	}

	private int				readLength	= -1;
	private int				dataLength	= 0;
	private ByteBuffer			streamBuffer	= null;

	public boolean read() throws IOException {
		
		if (readLength == -1) {
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
	
	public int getStreamLength(){
		return dataLength;
	}
}
