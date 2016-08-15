package com.gifisan.nio.component.protocol.http11.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.future.AbstractIOReadFuture;

public class WebSocketReadFutureImpl extends AbstractIOReadFuture implements WebSocketReadFuture{

	private boolean eof;
	
	private int type;
	
	private boolean hasMask;
	
	private int length;
	
	private ByteBuffer header;
	
	private ByteBuffer lengthBuffer;
	
	private ByteBuffer maskBuffer;
	
	private ByteBuffer dataBuffer;
	
	private boolean headerComplete;
	
	private boolean lengthComplete;
	
	private boolean maskComplete;
	
	private boolean dataComplete;
	
	private BufferedOutputStream data;
	
	private String serviceName;
	
	public WebSocketReadFutureImpl(Session session,ByteBuffer header) {
		super(session);
		
		this.header = header;
		
		this.serviceName = (String) session.getAttribute(SESSION_KEY_SERVICE_NAME);
		
		if (header.position() == HEADER_LENGTH) {
			doHeaderComplete(header);
		}
	}
	
	public WebSocketReadFutureImpl(Session session) {
		super(session);
	}
	
	private void doHeaderComplete(ByteBuffer header){
		
		byte [] array = header.array();
		
		byte b = array[0];
		
		eof = ((b & 0xFF) >> 7) == 1;
		
		type = (b & 0xF); 
		
		b = array[1];
		
		hasMask = ((b & 0xFF) >> 7) == 1;
		
		if (hasMask) {
			
			maskBuffer = ByteBuffer.allocate(4);
			
			maskComplete = false;
		}
		
		length = (b & 0x7f);
		
		if (length < 126) {

			doLengthComplete();

		}else if(length == 126){
			
			lengthBuffer = ByteBuffer.allocate(2);
		
		}else{
			
			lengthBuffer = ByteBuffer.allocate(4);
		}
		
		headerComplete = true;
	}

	public boolean read() throws IOException {
		
		TCPEndPoint endPoint = this.endPoint;
		
		if (!headerComplete) {
			
			endPoint.read(header);
			
			if (header.position() != HEADER_LENGTH) {
				return false;
			}
			
			doHeaderComplete(header);
		}
		
		if (!lengthComplete) {
			
			endPoint.read(lengthBuffer);
			
			if (lengthBuffer.hasRemaining()) {
				return false;
			}
			
			if (length == 126) {
				
				length = MathUtil.byte2IntFrom2Byte(lengthBuffer.array(), 0);
			}else{
				
				byte [] array = lengthBuffer.array();
				
				if ((array[0] >> 7) == -1) {
					// 欺负java没有无符号整型?
					throw new IOException("illegal data length:"+MathUtil.getHexString(array));
				}
				
				length = MathUtil.byte2Int(array);
			}
			
			doLengthComplete();
		}
		
		if (!maskComplete) {
			
			endPoint.read(maskBuffer);
			
			if (maskBuffer.hasRemaining()) {
				return false;
			}
			
			maskComplete = true;
		}
		
		if (!dataComplete) {
			
//			int length = endPoint.read(dataBuffer);
//			
//			data.write(dataBuffer.array(), 0, length);
			
			endPoint.read(dataBuffer);
			
			if (dataBuffer.hasRemaining()) {
				return false;
			}
			
			data = new BufferedOutputStream(dataBuffer.array());
			
			if (hasMask) {
				
				byte [] array = data.array();
				
				byte [] mask = maskBuffer.array();
				
				for (int i = 0; i < array.length; i++) {
					
					array[i] = (byte)(array[i] ^ mask[i % 4]);
				}
			}
			
			dataComplete = true;
			
			return true;
		}
		
		return true;
	}
	
	private void doLengthComplete(){
		
		
//		if (length > 1024 * 4) {
//			dataBuffer = ByteBuffer.allocate(1024 * 4);
//		}else{
//			dataBuffer = ByteBuffer.allocate(length);
//		}
		
		//FIXME 这里不应该直接allocate 
		dataBuffer = ByteBuffer.allocate(length);
		
//		data = new BufferedOutputStream(length);
		
		lengthComplete = true;
	}
	
	public String getServiceName() {
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

	public BufferedOutputStream getData() {
		return data;
	}
	
}
