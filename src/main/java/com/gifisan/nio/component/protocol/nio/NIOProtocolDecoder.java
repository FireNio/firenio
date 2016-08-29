package com.gifisan.nio.component.protocol.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.IOReadFuture;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.nio.future.MultiReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOBeatReadFuture;
import com.gifisan.nio.component.protocol.nio.future.StreamReadFuture;
import com.gifisan.nio.component.protocol.nio.future.TextReadFuture;

/**
 * <pre>
 *  B0 - B10:
 * 
 *  B0：
 *  +-----------------------------------+
 *  |                 B0                |
 *  +   -   -   -   -   -   -   -   -   +
 *  |   0   1   2   3   4   5   6   7   | 
 *  |   -   -   -   -   -   -   -   -   + 
 *  |  T Y P E|      Service  Name      |
 *  +-----------------------------------+
 *  
 *  Type:高两位，类型 [0=TEXT，1=STREAM，2=MULTI, 3=BEAT]
 *  ServiceName:低六位，service name的长度
 *  
 *  B1 - B3 ：future id
 *  B4 - B6 ：text content的长度
 *  B7 - B10：stream content的长度
 * 
 * </pre>
 */
public class NIOProtocolDecoder implements ProtocolDecoder {
	
	public static final byte	TYPE_HTTP					= 71;
	public static final byte	TYPE_BEAT					= 3;
	public static final byte	TYPE_MULTI					= 2;
	public static final byte	TYPE_STREAM					= 1;
	public static final byte	TYPE_TEXT					= 0;
	public static final int	PROTOCOL_HADER				= 11;
	public static final int	FUTUREID_BEGIN_INDEX			= 1;
	public static final int	STREAM_BEGIN_INDEX			= 7;
	public static final int	TEXT_BEGIN_INDEX				= 4;

	public IOReadFuture decode(TCPEndPoint endPoint) throws IOException {

		ByteBuffer header = ByteBuffer.allocate(PROTOCOL_HADER);

		int length = endPoint.read(header);

		if (length < 1) {
			if (length == -1) {
				CloseUtil.close(endPoint);
			}
			return null;
		}

		byte _type = header.get(0);
		
		int type = (_type & 0xff) >> 6;
		
		return doDecode(endPoint, header, type);
	}

	private IOReadFuture doDecode(TCPEndPoint endPoint, ByteBuffer header, int type) throws IOException {

		if (type == TYPE_TEXT) {
			return new TextReadFuture(endPoint.getSession(), header);
		} else if (type == TYPE_MULTI) {
			return new MultiReadFuture(endPoint.getSession(), header);
		} else if(type == TYPE_STREAM){
			return new StreamReadFuture(endPoint.getSession(), header);
		} else if(type == TYPE_BEAT){
			return new NIOBeatReadFuture(endPoint.getSession());
		}else {
			throw new IOException("not happen");
		}
	}
}
