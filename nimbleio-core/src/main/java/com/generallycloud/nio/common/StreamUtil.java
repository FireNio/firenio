package com.generallycloud.nio.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.ByteArrayInputStream;
import com.generallycloud.nio.component.SocketChannel;

public class StreamUtil {

	public static void write(InputStream inputStream, SocketChannel channel, int start, int length, int block)
			throws IOException {
		
		if (inputStream.getClass() == ByteArrayInputStream.class) {
			ByteArrayInputStream byteArray = (ByteArrayInputStream)inputStream;
			byte[] bytes = byteArray.toByteArray();
			channel.write(ByteBuffer.wrap(bytes));
			return;
		}

		inputStream.skip(start);
		
		if (block > length) {
			byte[] bytes = new byte[length];
			inputStream.read(bytes);
			channel.write(ByteBuffer.wrap(bytes));
		} else {
			byte[] bytes = new byte[block];
			int times = length / block;
			int remain = length % block;
			while (times > 0) {
				inputStream.read(bytes);
				channel.write(ByteBuffer.wrap(bytes));
				times--;
			}
			if (remain > 0) {
				inputStream.read(bytes, 0, remain);
				channel.write(ByteBuffer.wrap(bytes, 0, remain));
			}
		}
	}
}
