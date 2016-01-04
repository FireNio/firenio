package com.gifisan.mtp.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class NormalClientWriter implements ClientWriter {

	public void writeStream(ClientEndPoint endPoint,InputStream inputStream,ByteBuffer header,int block) throws IOException{
		endPoint.write(header);
		byte[] bytes = new byte[block];
		int length = inputStream.read(bytes);
		while (length == block) {
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			endPoint.write(buffer);
			length = inputStream.read(bytes);
		}
		if (length > 0) {
			ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, length);
			endPoint.write(buffer);
		}
		
	}
	
	public void writeText(ClientEndPoint endPoint,ByteBuffer buffer) throws IOException{
		endPoint.write(buffer);
	}

	public void writeBeat(ClientEndPoint endPoint) throws IOException {
		
	}
}
