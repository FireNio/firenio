package com.generallycloud.nio.component.protocol.fixedlength;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.IOWriteFuture;
import com.generallycloud.nio.component.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;

public class FixedLengthProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(SocketChannel endPoint, IOReadFuture future) throws IOException {
		
		if (future.isBeatPacket()) {
			
			byte [] array = MathUtil.int2Byte(-1);
			
			ByteBuf buffer = endPoint.getContext().getHeapByteBufferPool().allocate(4);
			
			buffer.put(array);
			
			buffer.flip();
			
			return new IOWriteFutureImpl(endPoint, future, buffer);
		}
		
		BufferedOutputStream outputStream = future.getWriteBuffer();
		
		int size = outputStream.size();
		
		ByteBuf buffer = endPoint.getContext().getHeapByteBufferPool().allocate(size + 4);
		
		byte [] size_array = MathUtil.int2Byte(size);
		
		buffer.put(size_array);
		
		buffer.put(outputStream.array(),0,size);
		
		buffer.flip();
		
		return new IOWriteFutureImpl(endPoint, future, buffer);
	}
}
