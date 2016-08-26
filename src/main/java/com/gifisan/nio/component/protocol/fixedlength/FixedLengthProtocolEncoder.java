package com.gifisan.nio.component.protocol.fixedlength;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.IOReadFuture;
import com.gifisan.nio.component.protocol.IOWriteFuture;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.TextWriteFuture;

public class FixedLengthProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(TCPEndPoint endPoint, IOReadFuture future) throws IOException {
		
		if (future.isBeatPacket()) {
			
			byte [] array = MathUtil.int2Byte(-1);
			
			ByteBuffer buffer = ByteBuffer.wrap(array);
			
			return new TextWriteFuture(endPoint, future, buffer);
		}
		
		BufferedOutputStream outputStream = future.getWriteBuffer();
		
		int size = outputStream.size();
		
		ByteBuffer buffer = ByteBuffer.allocate(size + 4);
		
		MathUtil.int2Byte(buffer.array(), size);
		
		buffer.position(4);
		
		buffer.put(outputStream.array(),0,size);
		
		buffer.flip();
		
		return new TextWriteFuture(endPoint, future, buffer);
	}
}
