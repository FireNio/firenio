package com.gifisan.nio.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.OutputStream;

public class StreamUtil {

	public static void write(InputStream inputStream, OutputStream outputStream, int start, int length, int block)
			throws IOException {

		inputStream.skip(start);

		if (block > length) {
			byte[] bytes = new byte[length];
			inputStream.read(bytes);
			outputStream.completedWrite(bytes);
		} else {
			byte[] bytes = new byte[block];
			int times = length / block;
			int remain = length % block;
			while (times > 0) {
				inputStream.read(bytes);
				outputStream.completedWrite(bytes);
				times--;
			}
			if (remain > 0) {
				inputStream.read(bytes, 0, remain);
				outputStream.completedWrite(bytes, 0, remain);
			}
		}
	}

	public static void write(com.gifisan.nio.component.InputStream inputStream, java.io.OutputStream outputStream,
			int block) throws IOException {

		int length = inputStream.available();

		if (block > length) {
			byte[] bytes = new byte[length];
			inputStream.completedRead(length);
			outputStream.write(bytes);
		} else {
			byte[] bytes = new byte[block];
			int times = length / block;
			int remain = length % block;
			while (times > 0) {
				inputStream.completedRead(block);
				outputStream.write(bytes);
				times--;
			}
			if (remain > 0) {
				inputStream.completedRead(remain);
				outputStream.write(bytes, 0, remain);
			}
		}
	}

	public static void write(InputStream inputStream, EndPoint endPoint, int start, int length, int block)
			throws IOException {

		inputStream.skip(start);

		if (block > length) {
			byte[] bytes = new byte[length];
			inputStream.read(bytes);
			endPoint.completedWrite(ByteBuffer.wrap(bytes));
		} else {
			byte[] bytes = new byte[block];
			int times = length / block;
			int remain = length % block;
			while (times > 0) {
				inputStream.read(bytes);
				endPoint.completedWrite(ByteBuffer.wrap(bytes));
				times--;
			}
			if (remain > 0) {
				inputStream.read(bytes, 0, remain);
				endPoint.completedWrite(ByteBuffer.wrap(bytes, 0, remain));
			}
		}
	}

	public static byte [] completeRead(com.gifisan.nio.component.InputStream inputStream) throws IOException{
		
		int allLength = inputStream.available();
		
		ByteBuffer buffer = ByteBuffer.allocate(allLength);
		
		inputStream.completedRead(buffer);
		
		return buffer.array();
	}
	
	
}
