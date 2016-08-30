package com.gifisan.nio.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.TCPEndPoint;

public class StreamUtil {

	public static void write(InputStream inputStream, TCPEndPoint endPoint, int start, int length, int block)
			throws IOException {
		
		if (inputStream.getClass() == ByteArrayInputStream.class) {
			ByteArrayInputStream byteArray = (ByteArrayInputStream)inputStream;
			byte[] bytes = byteArray.toByteArray();
			endPoint.write(ByteBuffer.wrap(bytes));
			return;
		}

		inputStream.skip(start);
		
		if (block > length) {
			byte[] bytes = new byte[length];
			inputStream.read(bytes);
			endPoint.write(ByteBuffer.wrap(bytes));
		} else {
			byte[] bytes = new byte[block];
			int times = length / block;
			int remain = length % block;
			while (times > 0) {
				inputStream.read(bytes);
				endPoint.write(ByteBuffer.wrap(bytes));
				times--;
			}
			if (remain > 0) {
				inputStream.read(bytes, 0, remain);
				endPoint.write(ByteBuffer.wrap(bytes, 0, remain));
			}
		}
	}
}
