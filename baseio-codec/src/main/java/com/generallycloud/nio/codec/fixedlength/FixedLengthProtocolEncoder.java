package com.generallycloud.nio.codec.fixedlength;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class FixedLengthProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(IOSession session, IOReadFuture future) throws IOException {
		
		if (future.isHeartbeat()) {
			
			byte [] array = MathUtil.int2Byte(future.isPING() 
					? FixedLengthProtocolDecoder.PROTOCOL_PING : 
						FixedLengthProtocolDecoder.PROTOCOL_PONG);
			
			ByteBuf buffer = session.getContext().getHeapByteBufferPool().allocate(4);
			
			buffer.put(array);
			
			buffer.flip();
			
			return new IOWriteFutureImpl(session, future, buffer);
		}
		
		BufferedOutputStream outputStream = future.getWriteBuffer();
		
		int size = outputStream.size();
		
		ByteBuf buffer = session.getContext().getHeapByteBufferPool().allocate(size + 4);
		
		byte [] size_array = MathUtil.int2Byte(size);
		
		buffer.put(size_array);
		
		buffer.put(outputStream.array(),0,size);
		
		buffer.flip();
		
		return new IOWriteFutureImpl(session, future, buffer);
	}
}
