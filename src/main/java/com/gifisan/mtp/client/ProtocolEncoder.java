package com.gifisan.mtp.client;

import java.nio.ByteBuffer;

public class ProtocolEncoder {

	
	
	private void encodeKS(byte [] header,int snLength){
		header[1]  = (byte)snLength;
	}
	
	private void encodeContent(byte [] header,int contentLength){
		header[2]  = (byte) ( contentLength          & 0xff);
		header[3]  = (byte) ((contentLength >>   8)  & 0xff);
		header[4]  = (byte) ((contentLength >>  16)  & 0xff);
	}
	
	private void encodeStream(byte [] header,int streamLength){
		header[5]  = (byte) ( streamLength          & 0xff);
		header[6]  = (byte) ((streamLength >>   8)  & 0xff);
		header[7]  = (byte) ((streamLength >>  16)  & 0xff);
		header[8]  = (byte) ( streamLength >>> 24);   
	}
	
	//text without content
	public ByteBuffer encode(byte[] serviceName){
		int snLength = serviceName.length;
		int bLength = snLength + 9;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		byte [] header = new byte[9];
		header[0]  = 0;
		encodeKS(header, snLength);
		
		buffer.put(header);
		buffer.put(serviceName);
		
		return buffer;
	}
	
	//text with content
	public ByteBuffer encode(byte[] serviceName,byte [] content){
		int snLength = serviceName.length;
		int contentLength = content.length;
		int bLength = snLength + contentLength + 9;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		byte [] header = new byte[9];
		header[0]  = 0;
		encodeKS(header, snLength);
		encodeContent(header,contentLength);
		
		buffer.put(header);
		buffer.put(serviceName);
		buffer.put(content);
		
		return buffer;
	}
	
	//data without content
	public ByteBuffer encode(byte[] serviceName,int streamLength){
		int snLength = serviceName.length;
		int bLength = snLength + 9;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		byte [] header = new byte[9];
		header[0]  = 1;
		encodeKS(header, snLength);
		encodeStream(header,streamLength);
		
		buffer.put(header);
		buffer.put(serviceName);
		return buffer;
	}
	
	//data with content
	public ByteBuffer encode(byte[] serviceName,byte [] content,int streamLength){
		int snLength = serviceName.length;
		int contentLength = content.length;
		int bLength = snLength + contentLength + 9;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		// >> 右移N位
		// << 左移N位
		byte [] header = new byte[9];
		header[0]  = 2;
		encodeKS(header, snLength);
		encodeContent(header,contentLength);
		encodeStream(header,streamLength);
		
		buffer.put(header);
		buffer.put(serviceName);
		buffer.put(content);
		return buffer;
	}
	
}
