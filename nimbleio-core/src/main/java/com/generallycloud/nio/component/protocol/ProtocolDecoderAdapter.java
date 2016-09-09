package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufferPool;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;

public abstract class ProtocolDecoderAdapter implements ProtocolDecoder {

	public IOReadFuture decode(TCPEndPoint endPoint) throws IOException {
		
		ByteBufferPool byteBufferPool = endPoint.getContext().getDirectByteBufferPool();

		ByteBuf buffer = allocate(byteBufferPool);

		int length = -1;

		try {

			length = buffer.read(endPoint);

		} finally {

			if (length < 1) {
				
				buffer.release();
				
				if (length == -1) {
					CloseUtil.close(endPoint);
				}
				
				return null;
			}
		}

		return fetchFuture(endPoint.getSession(), buffer);
	}
	
	protected abstract ByteBuf allocate(ByteBufferPool byteBufferPool);

	protected abstract IOReadFuture fetchFuture(Session session, ByteBuf buffer) ;

}