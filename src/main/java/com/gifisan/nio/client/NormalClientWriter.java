package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.StreamUtil;

public class NormalClientWriter implements ClientWriter {

	public void writeStream(ClientEndPoint endPoint, InputStream inputStream, int block) throws IOException {

		StreamUtil.write(inputStream, endPoint, 0, inputStream.available(), block);

	}

	public void writeText(ClientEndPoint endPoint, ByteBuffer buffer) throws IOException {
		endPoint.write(buffer);
	}

	public void writeBeat(ClientEndPoint endPoint) throws IOException {

	}
}
