package com.yoocent.mtp.component;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.yoocent.mtp.FlushedException;
import com.yoocent.mtp.OutputRangeException;
import com.yoocent.mtp.common.CloseUtil;
import com.yoocent.mtp.server.EndPoint;
import com.yoocent.mtp.server.InnerResponse;

public class MTPServletResponse implements InnerResponse{
	
	public static final byte RESPONSE_ERROR = -1;
	
	public static final byte RESPONSE_STREAM = 1;
	
	public static final byte RESPONSE_TEXT = 0;

	private int dataLength = 0;
	
	private EndPoint endPoint = null;
	
	private boolean flushed = false;
	
	private BufferedOutputStream response = new BufferedOutputStream();
	
	private byte type = RESPONSE_TEXT;
	
	private boolean typed = false;
	
	private int writedLength = 0;
	
	public MTPServletResponse(EndPoint endPoint) {
		this.endPoint = endPoint;
	}
	
	private static byte[] _empty_bytes = " ".getBytes();
	
	public void finish() throws IOException {
		if (type == RESPONSE_TEXT) {
			if (!flushed) {
				if (response.size() == 0) {
					this.write(_empty_bytes);
				}
				this.flush();
			}
		}else{
			if (writedLength != dataLength) {
				CloseUtil.close(endPoint);
				throw new OutputRangeException("writedLength:"+writedLength+",dataLength:"+dataLength);
			}
		}
	}
	
	public void flush() throws IOException {
		if (type < RESPONSE_STREAM) {
			this.flushText();
		}else{
			this.flushStream();
		}
	}

	private void flushStream() throws IOException{
		
		int _length = response.size();
		
		if (_length == 0) {
			throw new EOFException("empty byte");
		}
		
		if (_length + writedLength > dataLength) {
			throw new EOFException("max length: "+dataLength);
		}

		if (!endPoint.isOpened()) {
			throw new ChannelException("channel closed");
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(response.toByteArray());
		this.endPoint.write(buffer);
		this.response.reset();
		this.writedLength += _length;
		
	}
	
	private void flushText() throws IOException{
		if (flushed) {
			throw new FlushedException("flushed already");
		}
		
		if (response.size() == 0) {
			throw new EOFException("empty byte");
		}
		
		if (!endPoint.isOpened()) {
			throw new ChannelException("channel closed");
		}
		
		ByteBuffer buffer = getByteBufferTEXT();
		this.endPoint.write(buffer);
		this.response.reset();
		this.flushed = true;
	}
	
	private ByteBuffer getByteBufferStream(){
		byte [] header = new byte[5];
		
		header[0] = type;
		header[1] = (byte) ( dataLength          & 0xff);
		header[2] = (byte) ((dataLength >>   8)  & 0xff);
		header[3] = (byte) ((dataLength >>  16)  & 0xff);
		header[4] = (byte) ( dataLength >>> 24);   
		
		return ByteBuffer.wrap(header);
	}
	
	private ByteBuffer getByteBufferTEXT(){
		int length = this.response.size();
		byte [] header = new byte[5];
		
		header[0] = type;
		header[1] = (byte) ( length          & 0xff);
		header[2] = (byte) ((length >>   8)  & 0xff);
		header[3] = (byte) ((length >>  16)  & 0xff);
		header[4] = (byte) ( length >>> 24);   
		
		ByteBuffer buffer = ByteBuffer.allocate(length + 5);
		
		buffer.put(header);
		buffer.put(response.toByteArray());
		buffer.flip();
		
		return buffer;
	}
	
	public void setErrorResponse() throws IOException{
		if (typed) {
			throw new IOException("response typed");
		}
		this.type = RESPONSE_ERROR;
		this.typed = true;
	}
	
	public void setStreamResponse(int length) throws IOException{
		if (length < 1) {
			throw new EOFException("invalidate length");
		}
		
		if (typed) {
			throw new IOException("response typed");
		}
		

		this.type = RESPONSE_STREAM;
		
		this.dataLength = length;
		
		ByteBuffer buffer = getByteBufferStream();

		this.typed = true;
//		this.type = RESPONSE_STREAM;
//		this.dataLength = length;

		this.endPoint.write(buffer);
	}

	public void write(byte[] bytes) {
		this.response.write(bytes);
	}
	
	public void write(byte[] bytes, int offset, int length) {
		this.response.write(bytes, offset, length);
		
	}

	public void write(String content) {
		try {
			byte[] bytes = content.getBytes("UTF-8");
			this.write(bytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void write(String content, String encoding) {
		try {
			byte[] bytes = content.getBytes(encoding);
			this.write(bytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
}
