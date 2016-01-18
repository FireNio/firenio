package com.gifisan.mtp.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class AliveClientWriter extends NormalClientWriter implements ClientWriter{

	private byte [] beat = { 3 };
	
	public void writeStream(ClientEndPoint endPoint,InputStream inputStream,ByteBuffer header,int block) throws IOException{
		synchronized (this) {
			super.writeStream(endPoint, inputStream, header, block);
		}
	}
	
	public void writeBeat(ClientEndPoint endPoint) throws IOException{
		synchronized (this) {
			endPoint.write(beat);
		}
	}
	
}
