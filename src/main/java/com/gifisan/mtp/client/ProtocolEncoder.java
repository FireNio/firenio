package com.gifisan.mtp.client;

import java.nio.ByteBuffer;

public class ProtocolEncoder {

	
	
	private void encodeKS(byte [] header,int kLength,int sLength){
		header[1]  = (byte)sLength;
		header[2]  = (byte)kLength;
	}
	
	private void encodeContent(byte [] header,int pLength){
		header[3]  = (byte) ( pLength          & 0xff);
		header[4]  = (byte) ((pLength >>   8)  & 0xff);
		header[5]  = (byte) ((pLength >>  16)  & 0xff);
	}
	
	private void encodeStream(byte [] header,int streamLength){
		header[6]  = (byte) ( streamLength          & 0xff);
		header[7]  = (byte) ((streamLength >>   8)  & 0xff);
		header[8]  = (byte) ((streamLength >>  16)  & 0xff);
		header[9]  = (byte) ( streamLength >>> 24);   
	}
	
	//text without content
	public ByteBuffer encode(byte [] sessionID,byte[] serviceName){
		int sLength = sessionID.length;
		int kLength = serviceName.length;
		int bLength = sLength + kLength + 10;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		byte [] header = new byte[10];
		header[0]  = 0;
		encodeKS(header, kLength, sLength);
		
		buffer.put(header);
		buffer.put(sessionID);
		buffer.put(serviceName);
		
		return buffer;
	}
	
	//text with content
	public ByteBuffer encode(byte [] sessionID,byte[] serviceName,byte [] parameter){
		int sLength = sessionID.length;
		int kLength = serviceName.length;
		int pLength = parameter.length;
		int bLength = sLength + kLength + pLength + 10;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		byte [] header = new byte[10];
		header[0]  = 0;
		encodeKS(header, kLength, sLength);
		encodeContent(header,pLength);
		
		buffer.put(header);
		buffer.put(sessionID);
		buffer.put(serviceName);
		buffer.put(parameter);
		
		return buffer;
	}
	
	//data without content
	public ByteBuffer encode(byte [] sessionID,byte[] serviceName,int streamLength){
		int sLength = sessionID.length;
		int kLength = serviceName.length;
		int bLength = sLength + kLength + 10;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		byte [] header = new byte[10];
		header[0]  = 1;
		encodeKS(header, kLength, sLength);
		encodeStream(header,streamLength);
		
		buffer.put(header);
		buffer.put(sessionID);
		buffer.put(serviceName);
		return buffer;
	}
	
	//data with content
	public ByteBuffer encode(byte [] sessionID,byte[] serviceName,byte [] parameter,int streamLength){
		int sLength = sessionID.length;
		int kLength = serviceName.length;
		int pLength = parameter.length;
		int bLength = sLength + kLength + pLength + 10;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		// >> 右移N位
		// << 左移N位
		byte [] header = new byte[10];
		header[0]  = 2;
		encodeKS(header, kLength, sLength);
		encodeContent(header,pLength);
		encodeStream(header,streamLength);
		
		buffer.put(header);
		buffer.put(sessionID);
		buffer.put(serviceName);
		buffer.put(parameter);
		return buffer;
	}
	
}
