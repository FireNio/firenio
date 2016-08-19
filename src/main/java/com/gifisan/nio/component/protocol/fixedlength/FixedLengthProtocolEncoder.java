package com.gifisan.nio.component.protocol.fixedlength;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.future.IOWriteFuture;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.future.TextWriteFuture;

// >> 右移N位
// << 左移N位
public class FixedLengthProtocolEncoder implements ProtocolEncoder {

	@Override
	public IOWriteFuture encode(TCPEndPoint endPoint, ReadFuture future) throws IOException {
		
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
