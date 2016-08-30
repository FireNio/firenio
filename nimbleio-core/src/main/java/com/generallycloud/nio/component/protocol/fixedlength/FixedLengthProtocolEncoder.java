package com.generallycloud.nio.component.protocol.fixedlength;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.TCPEndPoint;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.IOWriteFuture;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.TextWriteFuture;

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
