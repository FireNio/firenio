package com.gifisan.mtp.component;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.mtp.FlushedException;
import com.gifisan.mtp.server.EndPoint;
import com.gifisan.mtp.server.OutputStream;
import com.gifisan.mtp.server.Response;

public class MTPServletResponse implements Response{
	
	private static byte emptyByte 				= ' ';
	public static final byte RESPONSE_ERROR 	= 0;
	public static final byte RESPONSE_STREAM 	= 2;
	public static final byte RESPONSE_TEXT 		= 1;
	private int dataLength 						= 0;
	private EndPoint endPoint 						= null;
	private boolean flushed 						= false;
	private byte type 								= RESPONSE_TEXT;
	private boolean typed 							= false;
	private OutputStream writer 					= new BufferedOutputStream();
	
	
	public MTPServletResponse(EndPoint endPoint) {
		this.endPoint = endPoint;
	}
	
	public void flush() throws IOException {
		if (type < RESPONSE_STREAM) {
			this.flushText();
		}
	}
	
	public void flushEmpty() throws IOException {
		this.endPoint.write(emptyByte);
		this.flush();
	}

//	private void flushStream() throws IOException{
//		
//		int _length = response.size();
//		
//		if (_length == 0) {
//			throw new EOFException("empty byte");
//		}
//		
//		if (_length + writedLength > dataLength) {
//			throw new EOFException("max length: "+dataLength);
//		}
//
//		if (!endPoint.isOpened()) {
//			throw new MTPChannelException("channel closed");
//		}
//		
//		ByteBuffer buffer = ByteBuffer.wrap(response.toByteArray());
//		this.endPoint.write(buffer);
//		this.response.reset();
//		this.writedLength += _length;
//		
//	}
	
	private void flushText() throws IOException{
		if (flushed) {
			throw new FlushedException("flushed already");
		}
		
		BufferedOutputStream _writer = (BufferedOutputStream) this.writer;
		
		if (_writer.size() == 0) {
			throw new EOFException("empty byte");
		}
		
		if (!endPoint.isOpened()) {
			throw new MTPChannelException("channel closed");
		}
		
		ByteBuffer buffer = getByteBufferTEXT();
		this.endPoint.write(buffer);
		_writer.reset();
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
		BufferedOutputStream _writer = (BufferedOutputStream) this.writer;
		int length = _writer.size();
		byte [] header = new byte[5];
		
		header[0] = type;
		header[1] = (byte) ( length          & 0xff);
		header[2] = (byte) ((length >>   8)  & 0xff);
		header[3] = (byte) ((length >>  16)  & 0xff);
		header[4] = (byte) ( length >>> 24);   
		
		ByteBuffer buffer = ByteBuffer.allocate(length + 5);
		
		buffer.put(header);
		buffer.put(_writer.toByteArray());
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
		this.writer = endPoint;
		this.dataLength = length;
		
		ByteBuffer buffer = getByteBufferStream();

		this.typed = true;
//		this.type = RESPONSE_STREAM;
//		this.dataLength = length;

		this.endPoint.write(buffer);
		this.writer = this.endPoint;
	}
	
	private OutputStream [] outputStreamWriters = new OutputStream[]{
			
			
			
	};

	public void write(byte b) throws IOException {
		this.writer.write(b);
		
	}

	public void write(byte[] bytes) throws IOException {
		this.writer.write(bytes);
	}
	
	public void write(byte[] bytes, int offset, int length) throws IOException {
		this.writer.write(bytes, offset, length);
		
	}

	public void write(String content) {
		try {
			byte[] bytes = content.getBytes("UTF-8");
			writer.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(String content, String encoding) {
		try {
			byte[] bytes = content.getBytes(encoding);
			writer.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
