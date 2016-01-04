package com.gifisan.mtp.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class AliveClientWriter extends NormalClientWriter implements ClientWriter{

	private byte [] beat = new byte[] { 3 };
	
	public void writeStream(ClientEndPoint endPoint,InputStream inputStream,ByteBuffer header,int block) throws IOException{
		synchronized (this) {
			super.writeStream(endPoint, inputStream, header, block);
		}
	}
	
	public void writeText(ClientEndPoint endPoint,ByteBuffer buffer) throws IOException{
		synchronized (this) {
			endPoint.write(buffer);
		}
	}
	
	public void writeBeat(ClientEndPoint endPoint) throws IOException{
		synchronized (this) {
			endPoint.write(beat);
		}
	}
	
}
