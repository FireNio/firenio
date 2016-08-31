package com.generallycloud.nio.component.protocol.nio.future;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.ChannelBufferOutputstream;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;

//FIXME 根据outputstream判断是否拷贝数据
public class MultiReadFuture extends AbstractNIOReadFuture {

	private int				dataLength;
	private ByteBuffer			streamBuffer;
	private int				readLength	= -1;
	private boolean			enableChannelBuffer;
	private static final Logger	logger		= LoggerFactory.getLogger(MultiReadFuture.class);

	public MultiReadFuture(Session session, ByteBuffer header) {
		super(session, header);
	}

	protected void decode(TCPEndPoint endPoint, byte[] header) {

		this.hasStream = true;

		this.dataLength = gainStreamLength(header);
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
			
			if (outputStream instanceof ChannelBufferOutputstream) {
				
				this.streamBuffer = ByteBuffer.allocate(dataLength);
				
				((ChannelBufferOutputstream)outputStream).setBuffer(streamBuffer);
				
				this.enableChannelBuffer = true;
				
			}else{
				
				int bufferLength = 1024 * 256;

				bufferLength = dataLength > bufferLength ? bufferLength : dataLength;

				this.streamBuffer = ByteBuffer.allocate(bufferLength);
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

		int length = buffer.position();

		if (length == 0) {
			return;
		}

		readLength += length;
		
		if (enableChannelBuffer) {
			return;
		}

		byte[] array = buffer.array();
		
		outputStream.write(array, 0, buffer.position());

		buffer.clear();
	}

	public int getStreamLength() {
		return dataLength;
	}

}
