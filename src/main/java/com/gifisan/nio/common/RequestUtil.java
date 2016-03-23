package com.gifisan.nio.common;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.InputStream;

public class RequestUtil {

	public static byte [] completeRead(InputStream inputStream) throws IOException{
		
		int allLength = inputStream.available();
		
		ByteBuffer buffer = ByteBuffer.allocate(allLength);
		
		inputStream.completedRead(buffer);
		
		return buffer.array();
	}
	
	
	
}
