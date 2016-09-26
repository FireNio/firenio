package com.generallycloud.nio.component.protocol.http11.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.protocol.AbstractIOReadFuture;
import com.generallycloud.nio.component.protocol.ProtocolException;
import com.generallycloud.nio.component.protocol.http11.WebSocketProtocolDecoder;

public class WebSocketReadFutureImpl extends AbstractIOReadFuture implements WebSocketReadFuture{
	
	protected int type;

	private boolean eof;
	
	private boolean hasMask;
	
	private int length;
	
	private ByteBuf buffer;
	
	private byte [] mask;
	
	private boolean headerComplete;
	
	private boolean remain_header_complete;
	
	private boolean dataComplete;
	
	private BufferedOutputStream data;
	
	private String serviceName;
	
	public WebSocketReadFutureImpl(Session session,ByteBuf buffer) {
		super(session);
		
		this.buffer = buffer;
		
		this.serviceName = (String) session.getAttribute(SESSION_KEY_SERVICE_NAME);
		
		if (!buffer.hasRemaining()) {
			doHeaderComplete(buffer);
		}
	}
	
	protected WebSocketReadFutureImpl(Session session) {
		super(session);
	}
	
	private void doHeaderComplete(ByteBuf buffer){
		
		headerComplete = true;
		
		int offset = buffer.offset();
		
		int remain_header_size = 0;
		
		byte [] array = buffer.array();
		
		byte b = array[offset + 0];
		
		eof = ((b & 0xFF) >> 7) == 1;
		
		type = (b & 0xF); 
		
		
		if (type == WebSocketProtocolDecoder.TYPE_PING) {
			setPING();
		}else if(type == WebSocketProtocolDecoder.TYPE_PONG){
			setPONG();
		}
		
		b = array[offset + 1];
		
		hasMask = ((b & 0xFF) >> 7) == 1;
		
		if (hasMask) {
			
			remain_header_size += 4;
		}
		
		length = (b & 0x7f);
		
		if (length < 126) {

			
		}else if(length == 126){
			
			remain_header_size += 2;
		
		}else{
			
			remain_header_size += 4;
		}
		
		buffer.limit(remain_header_size);
		
	}
	
	private void doRemainHeaderComplete(ByteBuf buffer) throws IOException{
		
		remain_header_complete = true;
		
		byte [] array = buffer.array();
		int offset = buffer.offset();
		if(length < 126){
			
			
		}else if (length == 126) {
			
			length = MathUtil.byte2IntFrom2Byte(array, offset);
			
		}else{
			
			if ((array[offset] >> 7) == -1) {
				// 欺负java没有无符号整型?
				throw new IOException("illegal data length ,unsigned integer");
			}
			
			length = MathUtil.byte2Int(array,offset);
		}
		
		mask = new byte[4];
		
		System.arraycopy(array, offset + buffer.limit() - 4, mask, 0, 4);
		
		doLengthComplete(buffer,length);
	}

	public boolean read() throws IOException {
		
		SocketChannel channel = this.channel;
		
		ByteBuf buffer = this.buffer;
		
		if (!headerComplete) {
			
			buffer.read(channel);
			
			if (buffer.hasRemaining()) {
				return false;
			}
			
			doHeaderComplete(buffer);
		}
		
		if (!remain_header_complete) {
			
			buffer.read(channel);
			
			if (buffer.hasRemaining()) {
				return false;
			}
			
			doRemainHeaderComplete(buffer);
		}
		
		if (!dataComplete) {
			
			buffer.read(channel);
			
			if (buffer.hasRemaining()) {
				return false;
			}
			
			buffer.flip();
			
			byte [] array = buffer.getBytes();
			
			if (hasMask) {
				
				byte [] mask = this.mask;
				
				for (int i = 0; i < array.length; i++) {
					
					array[i] = (byte)(array[i] ^ mask[i % 4]);
				}
			}
			
			this.data = new BufferedOutputStream(array);
			
			dataComplete = true;
			
			return true;
		}
		
		return true;
	}
	
	private void doLengthComplete(ByteBuf buffer,int length){
		
		if (length > 1024 * 8) {
			throw new ProtocolException("max 8KB ,length:" + length);
		}
		
		if (buffer.capacity() >= length) {
			buffer.limit(length);
			return;
		}
		
		ReleaseUtil.release(buffer);
		
		this.buffer = channel.getContext().getHeapByteBufferPool().allocate(length);
	}
	
	public String getFutureName() {
		return serviceName;
	}

	public boolean isEof() {
		return eof;
	}

	public int getType() {
		return type;
	}
	
	public int getLength() {
		return length;
	}
	
	public void release() {
		ReleaseUtil.release(buffer);
	}

	public BufferedOutputStream getData() {
		return data;
	}
	
}
