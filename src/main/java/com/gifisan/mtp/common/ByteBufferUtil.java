package com.gifisan.mtp.common;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ByteBufferUtil {
	
	private static final byte TYPE_DATA = 2;

	private static final byte TYPE_MULT = 3;

	private static final byte TYPE_TEXT = 1;
	
	public static ByteBuffer getByteBuffer(String sessionID,String serviceName,String parameter,int streamLength){
		if (streamLength < 1) {
			if (StringUtil.isNullOrBlank(parameter)) {
				return getByteBuffer_TYPE_TEXT(sessionID, serviceName);
			}else{
				return getByteBuffer_TYPE_TEXT(sessionID, serviceName,parameter);
			}
		}else{
			if (StringUtil.isNullOrBlank(parameter)) {
				return getByteBuffer_TYPE_DATA(sessionID, serviceName, streamLength);
			}else{
				return getByteBuffer_TYPE_MULT(sessionID, serviceName, parameter, streamLength);
			}
		}
		
	}
	
	private static ByteBuffer getByteBuffer_TYPE_TEXT(String sessionID,String serviceName) {
		
		byte [] sBytes = sessionID.getBytes();
		byte [] kBytes = serviceName.getBytes();
		
		int sLength = sBytes.length;
		int kLength = kBytes.length;
		int bLength = 0;
		
		bLength = sLength + kLength + 12;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		byte [] header = new byte[12];
		header[0]  = TYPE_TEXT;
		header[1]  = (byte)sLength;
		header[2]  = (byte)kLength;
		header[3]  = 0;
		header[4]  = 0;
		header[5]  = 0;
		header[6]  = 0;
		header[7]  = 0;
		header[8]  = 0;
		header[9]  = 0;
		header[10] = 0;
		header[11] = 0;
		
		
		buffer.put(header);
		buffer.put(sBytes);
		buffer.put(kBytes);
		
		return buffer;
	}

	private static ByteBuffer getByteBuffer_TYPE_TEXT(String sessionID,String serviceName,String parameter) {
		
		byte [] sBytes = sessionID.getBytes();
		byte [] kBytes = serviceName.getBytes();
		byte [] pBytes = null;
		
		
		int sLength = sBytes.length;
		int kLength = kBytes.length;
		int pLength = 0;
		int bLength = 0;
		
		try {
			pBytes = parameter.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		pLength = pBytes.length;
		
		bLength = sLength + kLength + pLength + 12;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		byte [] header = new byte[12];
		header[0]  = TYPE_TEXT;
		header[1]  = (byte)sLength;
		header[2]  = (byte)kLength;
		header[3]  = 0;
		header[4]  = 0;
		header[5]  = (byte) ( pLength          & 0xff);
		header[6]  = (byte) ((pLength >>   8)  & 0xff);
		header[7]  = (byte) ((pLength >>  16)  & 0xff);
		header[8]  = 0;
		header[9]  = 0;
		header[10] = 0;
		header[11] = 0;
		
		
		buffer.put(header);
		buffer.put(sBytes);
		buffer.put(kBytes);
		buffer.put(pBytes);
		
		return buffer;
	}
	
	private static ByteBuffer getByteBuffer_TYPE_DATA(String sessionID,String serviceName,int streamLength) {
		
		byte [] sBytes = sessionID.getBytes();
		byte [] kBytes = serviceName.getBytes();
		
		int sLength = sBytes.length;
		int kLength = kBytes.length;
		int bLength = 0;
		
		bLength = sLength + kLength + 12;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		byte [] header = new byte[12];
		header[0]  = TYPE_DATA;
		header[1]  = (byte)sLength;
		header[2]  = (byte)kLength;
		header[3]  = 0;
		header[4]  = 0;
		header[5]  = 0;
		header[6]  = 0;
		header[7]  = 0;
		header[8]  = (byte) ( streamLength          & 0xff);
		header[9]  = (byte) ((streamLength >>   8)  & 0xff);
		header[10] = (byte) ((streamLength >>  16)  & 0xff);
		header[11] = (byte) ( streamLength >>> 24);   
		
		buffer.put(header);
		buffer.put(sBytes);
		buffer.put(kBytes);
		return buffer;
	}

	private static ByteBuffer getByteBuffer_TYPE_MULT(String sessionID,String serviceName,String parameter,int streamLength){
		
		byte [] sBytes = sessionID.getBytes();
		byte [] kBytes = serviceName.getBytes();
		byte[] pBytes = null;
		try {
			pBytes = parameter.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			
		}
		
		int sLength = sBytes.length;
		int kLength = kBytes.length;
		int pLength = pBytes.length;
		int bLength = 0;
		
		bLength = sLength + kLength + pLength + 12;
		
		ByteBuffer buffer = ByteBuffer.allocate(bLength);
		
		// >> 右移N位
		// << 左移N位
		byte [] header = new byte[12];
		header[0]  = TYPE_MULT;
		header[1]  = (byte)sLength;
		header[2]  = (byte)kLength;
		header[3]  = 0;
		header[4]  = 0;
		header[5]  = (byte) ( pLength               & 0xff);
		header[6]  = (byte) ((pLength      >>   8)  & 0xff);
		header[7]  = (byte) ((pLength      >>  16)  & 0xff);
		header[8]  = (byte) ( streamLength          & 0xff);
		header[9]  = (byte) ((streamLength >>   8)  & 0xff);
		header[10] = (byte) ((streamLength >>  16)  & 0xff);
		header[11] = (byte) ( streamLength >>> 24);         
		
		buffer.put(header);
		buffer.put(sBytes);
		buffer.put(kBytes);
		buffer.put(pBytes);
		return buffer;
	}
	
}
