package com.generallycloud.nio.codec.http11.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http11.WebSocketProtocolDecoder;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;
import com.generallycloud.nio.protocol.ProtocolException;

public class WebSocketReadFutureImpl extends AbstractIOReadFuture implements WebSocketReadFuture{
	
	protected int type;

	private boolean eof;
	
	private boolean hasMask;
	
	private int length;
	
	private ByteBuf buf;
	
	private byte [] mask;
	
	private boolean headerComplete;
	
	private boolean remain_header_complete;
	
	private boolean dataComplete;
	
	private BufferedOutputStream data;
	
	private String serviceName;
	
	public WebSocketReadFutureImpl(SocketSession session,ByteBuf buf) {
		super(session.getContext());
		
		this.buf = buf;
		
		this.serviceName = (String) session.getAttribute(SESSION_KEY_SERVICE_NAME);
	}
	
	public WebSocketReadFutureImpl(BaseContext context) {
		super(context);
	}
	
	private void doHeaderComplete(ByteBuf buf){
		
		headerComplete = true;
		
		int offset = buf.offset();
		
		int remain_header_size = 0;
		
		byte [] array = buf.array();
		
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
		
		buf.limit(remain_header_size);
		
	}
	
	private void doRemainHeaderComplete(SocketSession session,ByteBuf buf) throws IOException{
		
		remain_header_complete = true;
		
		byte [] array = buf.array();
		int offset = buf.offset();
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
		
		System.arraycopy(array, offset + buf.limit() - 4, mask, 0, 4);
		
		doLengthComplete(session,buf,length);
	}

	public boolean read(SocketSession session,ByteBuf buffer) throws IOException {
		
		ByteBuf buf = this.buf;
		
		if (!headerComplete) {
			
			buf.read(buffer);
			
			if (buf.hasRemaining()) {
				return false;
			}
			
			doHeaderComplete(buf);
		}
		
		if (!remain_header_complete) {
			
			buf.read(buffer);
			
			if (buf.hasRemaining()) {
				return false;
			}
			
			doRemainHeaderComplete(session,buf);
		}
		
		if (!dataComplete) {
			
			buf.read(buffer);
			
			if (buf.hasRemaining()) {
				return false;
			}
			
			buf.flip();
			
			byte [] array = buf.getBytes();
			
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
	
	private void doLengthComplete(SocketSession session,ByteBuf buf,int length){
		
		if (length > 1024 * 8) {
			throw new ProtocolException("max 8KB ,length:" + length);
		}
		
		if (buf.capacity() >= length) {
			buf.limit(length);
			return;
		}
		
		ReleaseUtil.release(buf);
		
		this.buf = allocate(length);
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
		ReleaseUtil.release(buf);
	}

	public BufferedOutputStream getData() {
		return data;
	}
	
}
