package com.generallycloud.nio.component.protocol.nio.future;

import java.io.IOException;
import java.io.OutputStream;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.ChannelBufferOutputstream;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;

//FIXME 根据outputstream判断是否拷贝数据
public class MultiReadFuture extends AbstractNIOReadFuture {

	private int				dataLength;
	private int				readLength	= -1;
	private boolean			enableChannelBuffer;
	private static final Logger	logger		= LoggerFactory.getLogger(MultiReadFuture.class);

	public MultiReadFuture(Session session, ByteBuf header) throws IOException {
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
				eventHandle.accept(session, this);
			} catch (Exception e) {
				logger.debug(e);
			}

			if (this.outputStream == null) {
				throw new IOException("none outputstream");
			}
			
			if (outputStream instanceof ChannelBufferOutputstream) {
				
				ByteBuf buffer = this.buffer;
				
				if (buffer.capacity() > dataLength) {
					
					buffer.limit(dataLength);
				}else{
					
					buffer.release();
					
					this.buffer = endPoint.getContext().getDirectByteBufferPool().allocate(dataLength);
				}
				
				((ChannelBufferOutputstream)outputStream).setBuffer(buffer);
				
				this.enableChannelBuffer = true;
				
			}else{
				
				int bufferLength = 1024 * 8;

				bufferLength = dataLength > bufferLength ? bufferLength : dataLength;
				
				if (buffer.capacity() > bufferLength) {
					
					buffer.limit(bufferLength);
				}else{
					
					buffer.release();
					
					this.buffer = endPoint.getContext().getDirectByteBufferPool().allocate(bufferLength);
				}
			}
			
			readLength = 0;
		}

		if (readLength < dataLength) {
			
			ByteBuf buffer = this.buffer;

			buffer.read(endPoint);
			
			fill(outputStream, buffer);
		}

		if (readLength == dataLength) {
			
			buffer.release();
			
			return true;
		}
		
		return false;
	}

	private void fill(OutputStream outputStream, ByteBuf buffer) throws IOException {

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
