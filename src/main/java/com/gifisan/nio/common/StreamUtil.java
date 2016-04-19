package com.gifisan.nio.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.client.ClientEndPoint;
import com.gifisan.nio.client.EndPointInputStream;
import com.gifisan.nio.component.ByteArrayInputStream;

public class StreamUtil {

	public static void write(EndPointInputStream inputStream, OutputStream outputStream,
			int block) throws IOException {

		int length = inputStream.available();

		if (block > length) {
			byte[] bytes = new byte[length];
			inputStream.read(length);
			outputStream.write(bytes);
		} else {
			byte[] bytes = new byte[block];
			int times = length / block;
			int remain = length % block;
			while (times > 0) {
				inputStream.read(block);
				outputStream.write(bytes);
				times--;
			}
			if (remain > 0) {
				inputStream.read(remain);
				outputStream.write(bytes, 0, remain);
			}
		}
	}

	public static void write(InputStream inputStream, ClientEndPoint endPoint, int start, int length, int block)
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

	public static byte [] completeRead(EndPointInputStream inputStream) throws IOException{
		
		int allLength = inputStream.available();
		
		ByteBuffer buffer = ByteBuffer.allocate(allLength);
		
		inputStream.read(buffer);
		
		return buffer.array();
	}
	
	
}
