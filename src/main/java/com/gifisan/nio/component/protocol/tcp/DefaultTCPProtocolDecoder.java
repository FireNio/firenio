package com.gifisan.nio.component.protocol.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.MultiReadFuture;
import com.gifisan.nio.component.future.StreamReadFuture;
import com.gifisan.nio.component.future.TextReadFuture;

public class DefaultTCPProtocolDecoder implements ProtocolDecoder {

	public IOReadFuture decode(TCPEndPoint endPoint) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(1);

		int length = endPoint.read(buffer);

		if (length < 1) {
			if (length < 0) {
				endPoint.endConnect();
			}
			return null;
		}

		byte type = buffer.get(0);

		if (type < 3) {

			if (type < 0) {
				return null;
			}

			return this.doDecode(endPoint, type);

		} else {

			return this.doDecodeExtend(endPoint, type);
		}
	}

	public IOReadFuture decodeMulti(TCPEndPoint endPoint, byte[] header) throws IOException {
		
		byte sessionID = gainSessionID(header);
		
		int textLength = gainTextLength(header);
		
		int dataLength = gainStreamLength(header);

		Session session = endPoint.getSession(sessionID);
		
		ByteBuffer textBuffer = ByteBuffer.allocate(textLength);
		
		String serviceName = gainServiceName(endPoint, header);
		
		return new MultiReadFuture(endPoint,textBuffer, session, serviceName,dataLength);
	}

	public IOReadFuture decodeStream(TCPEndPoint endPoint, byte[] header) throws IOException {

		byte sessionID = gainSessionID(header);

		int dataLength = gainStreamLength(header);

		Session session = endPoint.getSession(sessionID);

		String serviceName = gainServiceName(endPoint, header);

		return new StreamReadFuture(endPoint, session, serviceName, dataLength);
	}
	
	public IOReadFuture decodeText(TCPEndPoint endPoint, byte[] header) throws IOException {

		byte sessionID = gainSessionID(header);
		
		int textLength = gainTextLength(header);

		Session session = endPoint.getSession(sessionID);
		
		ByteBuffer textBuffer = ByteBuffer.allocate(textLength);
		
		String serviceName = gainServiceName(endPoint, header);
		
		return new TextReadFuture(endPoint,textBuffer, session, serviceName);
		
	}
	
	private IOReadFuture doDecode(TCPEndPoint endPoint, byte type) throws IOException {

		byte[] header = readHeader(endPoint);

		if (header == null) {
			endPoint.endConnect();
			return null;
		}
		
		if (type == TEXT) {
			return decodeText(endPoint, header);
		}else if(type == MULTI){
			return decodeMulti(endPoint, header);
		}else{
			return decodeStream(endPoint, header);
		}
	}
	
	public IOReadFuture doDecodeExtend(TCPEndPoint endPoint, byte type) throws IOException {

		return null;
	}

	private String gainServiceName(TCPEndPoint endPoint, byte[] header) throws IOException {

		int serviceNameLength = header[1];
		
		if (serviceNameLength == 0) {

			throw new IOException("service name is empty");
		}

		ByteBuffer buffer = endPoint.read(serviceNameLength);

		byte[] bytes = buffer.array();

		return new String(bytes, 0, serviceNameLength);
	}
	
	private byte gainSessionID(byte[] header) throws IOException {

		byte sessionID = header[0];

		if (sessionID > 3 || sessionID < 0) {
			throw new IOException("invalidate session id");
		}

		return sessionID;

	}
	
	private int gainStreamLength(byte[] header) {
		int v0 = (header[5] & 0xff);
		int v1 = (header[6] & 0xff) << 8;
		int v2 = (header[7] & 0xff) << 16;
		int v3 = (header[8] & 0xff) << 24;
		return v0 | v1 | v2 | v3;
	}

	private int gainTextLength(byte[] header) {
		int v0 = (header[2] & 0xff);
		int v1 = (header[3] & 0xff) << 8;
		int v2 = (header[4] & 0xff) << 16;
		return v0 | v1 | v2;
	}
	
	private byte[] readHeader(TCPEndPoint endPoint) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(9);

		int length = endPoint.read(buffer);

		if (length < 9) {
			// 如果一次读取不到9个byte
			// 这样的连接持续下去也是无法进行业务操作

			return null;
		}

		return buffer.array();
	}

}
