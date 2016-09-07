package com.generallycloud.nio.component.protocol.fixedlength.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufferPool;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.AbstractIOReadFuture;
import com.generallycloud.nio.component.protocol.ProtocolException;
import com.generallycloud.nio.component.protocol.fixedlength.FixedLengthProtocolDecoder;

public class FixedLengthReadFutureImpl extends AbstractIOReadFuture implements FixedLengthReadFuture {

	private ByteBuf	buf;

	private String		text;

	private int		length;

	private boolean	header_complete;

	private boolean	body_complete;

	private byte[]	byteArray;

	private int		limit	= 1024 * 1024;

	public FixedLengthReadFutureImpl(Session session, ByteBuf buf) {
		
		super(session);
		
		this.buf = buf;

		if (!isHeaderReadComplete(buf)) {
			doHeaderComplete(buf);
		}
	}

	public FixedLengthReadFutureImpl(Session session) {
		super(session);
	}
	
	protected FixedLengthReadFutureImpl(Session session,boolean isBeatPacket) {
		super(session);
		this.isBeatPacket = isBeatPacket;
	}
	
	private boolean isHeaderReadComplete(ByteBuf buf){
		return buf.position() > FixedLengthProtocolDecoder.PROTOCOL_HADER;
	}

	private void doHeaderComplete(ByteBuf buf) {

		header_complete = true;
		
		ByteBufferPool bufferPool = endPoint.getContext().getDirectByteBufferPool();
		
//		ByteBuf temp = bufferPool.poll(4);
//		
//		buffer.getBytes(temp.array());
		
//		int length = MathUtil.byte2Int(temp.array());
		
		int length = buf.getInt(0);
		
		this.length = length;
		
		if (length < 1) {
			
			if (length == -1) {
			
				isBeatPacket = true;
				
				body_complete = true;
				
//				temp.release();
				
				return;
			}
			
			throw new ProtocolException("illegal length:" + length);
			
		}else if (length > limit) {
			
			throw new ProtocolException("max 1M ,length:" + length);
			
		}else if(length > buf.capacity()){
			
			buf.release();
			
			this.buf = bufferPool.poll(length);
			
		}else{
			
			buf.clear();
			
			buf.limit(length);
		}
	}

	public boolean read() throws IOException {

		if (!header_complete) {

			buf.read(endPoint);

			if (!isHeaderReadComplete(buf)) {
				return false;
			}

			doHeaderComplete(buf);
		}

		if (!body_complete) {

			buf.read(endPoint);

			if (buf.hasRemaining()) {
				return false;
			}

			doBodyComplete(buf);
		}
		
		buf.release();

		return true;
	}

	private void doBodyComplete(ByteBuf buf) {

		body_complete = true;

		byteArray = new byte[buf.limit()];
		
		buf.flip();
		
		buf.getBytes(byteArray);
	}

	public String getServiceName() {
		return null;
	}

	public String getText() {

		if (text == null) {
			text = new String(byteArray, session.getContext().getEncoding());
		}

		return text;
	}
	
	public int getLength() {
		return length;
	}

	public byte[] getByteArray() {
		return byteArray;
	}
}
