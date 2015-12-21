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
	public static final byte RESPONSE_STREAM 	= 1;
	public static final byte RESPONSE_TEXT 		= 0;
	private int dataLength 						= 0;
	private EndPoint endPoint 						= null;
	private boolean flushed 						= false;
	private byte type 								= RESPONSE_TEXT;
	private boolean typed 							= false;
	private BufferedOutputStream bufferWriter 		= new BufferedOutputStream();
	private OutputStream writer 					= null;
	
	
	
	public MTPServletResponse(EndPoint endPoint) {
		this.endPoint = endPoint;
		this.writer   = this.bufferWriter;
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
		
		if (bufferWriter.size() == 0) {
			throw new EOFException("empty byte");
		}
		
		if (!endPoint.isOpened()) {
			throw new MTPChannelException("channel closed");
		}
		
		ByteBuffer buffer = getByteBufferTEXT();
		this.endPoint.write(buffer);
		this.bufferWriter.reset();
		this.flushed = true;
	}
	
	private ByteBuffer getByteBufferStream(){
		byte [] header = new byte[5];
		int _dataLength = dataLength;
		
		header[0] = type;
		header[1] = (byte) ( _dataLength          & 0xff);
		header[2] = (byte) ((_dataLength >>   8)  & 0xff);
		header[3] = (byte) ((_dataLength >>  16)  & 0xff);
		header[4] = (byte) ( _dataLength >>> 24);   
		
		return ByteBuffer.wrap(header);
	}
	
	private ByteBuffer getByteBufferTEXT(){
		int length = bufferWriter.size();
		byte [] header = new byte[5];
		
		header[0] = type;
		header[1] = (byte) ( length          & 0xff);
		header[2] = (byte) ((length >>   8)  & 0xff);
		header[3] = (byte) ((length >>  16)  & 0xff);
		header[4] = (byte) ( length >>> 24);   
		
		ByteBuffer buffer = ByteBuffer.allocate(length + 5);
		
		buffer.put(header);
		buffer.put(bufferWriter.toByteArray());
		buffer.flip();
		
		return buffer;
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
		this.endPoint.write(buffer);
		this.writer = this.endPoint;
	}
	
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
