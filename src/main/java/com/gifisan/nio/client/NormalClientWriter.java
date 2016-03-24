package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class NormalClientWriter implements ClientWriter {

	public void writeStream(ClientEndPoint endPoint,InputStream inputStream,int block) throws IOException{
		byte[] bytes = new byte[block];
		int length = inputStream.read(bytes);
		for (;length == block;) {
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			endPoint.completedWrite(buffer);
			length = inputStream.read(bytes);
		}
		if (length > 0) {
			ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, length);
			endPoint.completedWrite(buffer);
		}
		
	}
	
	public void writeText(ClientEndPoint endPoint,ByteBuffer buffer) throws IOException{
		endPoint.completedWrite(buffer);
	}

	public void writeBeat(ClientEndPoint endPoint) throws IOException {
		
	}
}
