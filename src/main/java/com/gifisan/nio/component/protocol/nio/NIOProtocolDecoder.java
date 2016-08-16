package com.gifisan.nio.component.protocol.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.future.IOReadFuture;
import com.gifisan.nio.component.protocol.nio.future.MultiReadFuture;
import com.gifisan.nio.component.protocol.nio.future.StreamReadFuture;
import com.gifisan.nio.component.protocol.nio.future.TextReadFuture;

/**
 * <pre>
 * [0       ~              11]
 *  0         = 类型 [0=TEXT，1=STREAM，2=MULTI]
 *  1,2,3     = request id的长度
 *  4,        = service name的长度
 *  5,6,7     = text content的长度
 *  8,9,10,11 = stream content的长度
 * </pre>
 */
public class NIOProtocolDecoder implements ProtocolDecoder {
	
	public static final byte	TYPE_HTTP					= 71;
	public static final byte	TYPE_MULTI					= 2;
	public static final byte	TYPE_STREAM					= 1;
	public static final byte	TYPE_TEXT					= 0;
	public static final int	PROTOCOL_HADER				= 12;
	public static final int	SERVICE_NAME_LENGTH_INDEX		= 4;
	public static final int	STREAM_BEGIN_INDEX			= 8;
	public static final int	TEXT_BEGIN_INDEX				= 5;

	public IOReadFuture decode(TCPEndPoint endPoint) throws IOException {

		ByteBuffer header = ByteBuffer.allocate(PROTOCOL_HADER);

		int length = endPoint.read(header);

		if (length < 1) {
			if (length == -1) {
				CloseUtil.close(endPoint);
			}
			return null;
		}

		byte type = header.get(0);
		
		return doDecode(endPoint, header, type);
	}

	private IOReadFuture doDecode(TCPEndPoint endPoint, ByteBuffer header, byte type) throws IOException {

		if (type == TYPE_TEXT) {
			return new TextReadFuture(endPoint.getSession(), header);
		} else if (type == TYPE_MULTI) {
			return new MultiReadFuture(endPoint.getSession(), header);
		} else if(type == TYPE_STREAM){
			return new StreamReadFuture(endPoint.getSession(), header);
		}else {
			return this.doDecodeExtend(endPoint, header, type);
		}
	}

	public IOReadFuture doDecodeExtend(TCPEndPoint endPoint, ByteBuffer header, byte type) throws IOException {
		return null;
	}

}
