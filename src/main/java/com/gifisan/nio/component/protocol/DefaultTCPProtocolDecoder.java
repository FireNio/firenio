package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.MathUtil;
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
		
		int textLength = gainTextLength(header);
		
		int dataLength = gainStreamLength(header);

		Session session = endPoint.getSession();
		
		ByteBuffer textBuffer = ByteBuffer.allocate(textLength);
		
		String serviceName = gainServiceName(endPoint, header);
		
		return new MultiReadFuture(endPoint,textBuffer, session, serviceName,dataLength);
	}

	public IOReadFuture decodeStream(TCPEndPoint endPoint, byte[] header) throws IOException {

		int dataLength = gainStreamLength(header);

		Session session = endPoint.getSession();

		String serviceName = gainServiceName(endPoint, header);

		return new StreamReadFuture(endPoint, session, serviceName, dataLength);
	}
	
	public IOReadFuture decodeText(TCPEndPoint endPoint, byte[] header) throws IOException {

		int textLength = gainTextLength(header);

		Session session = endPoint.getSession();
		
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
		
		if (type == TYPE_TEXT) {
			return decodeText(endPoint, header);
		}else if(type == TYPE_MULTI){
			return decodeMulti(endPoint, header);
		}else{
			return decodeStream(endPoint, header);
		}
	}
	
	public IOReadFuture doDecodeExtend(TCPEndPoint endPoint, byte type) throws IOException {

		return null;
	}

	private String gainServiceName(TCPEndPoint endPoint, byte[] header) throws IOException {

		int serviceNameLength = header[0];
		
		if (serviceNameLength == 0) {

			throw new IOException("service name is empty");
		}

		ByteBuffer buffer = endPoint.read(serviceNameLength);

		byte[] bytes = buffer.array();

		return new String(bytes, 0, serviceNameLength);
	}
	
	private int gainStreamLength(byte[] header) {
		return MathUtil.byte2Int(header, 4);
	}

	private int gainTextLength(byte[] header) {
		int v0 = (header[1] & 0xff);
		int v1 = (header[2] & 0xff) << 8;
		int v2 = (header[3] & 0xff) << 16;
		return v0 | v1 | v2;
	}
	
	//FIXME read protocol
	private byte[] readHeader(TCPEndPoint endPoint) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(8);

		int length = endPoint.read(buffer);

		if (length < 8) {
			// 如果一次读取不到8个byte
			// 这样的连接持续下去也是无法进行业务操作

			return null;
		}

		return buffer.array();
	}

}
