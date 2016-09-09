package com.generallycloud.nio.component.protocol.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufferPool;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolDecoderAdapter;
import com.generallycloud.nio.component.protocol.nio.future.MultiReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOBeatReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.StreamReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.TextReadFuture;

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
public class NIOProtocolDecoder extends ProtocolDecoderAdapter {
	
	public static final byte	TYPE_HTTP					= 71;
	public static final byte	TYPE_BEAT					= 3;
	public static final byte	TYPE_MULTI					= 2;
	public static final byte	TYPE_STREAM					= 1;
	public static final byte	TYPE_TEXT					= 0;
	public static final int	PROTOCOL_HADER				= 11;
	public static final int	FUTUREID_BEGIN_INDEX			= 1;
	public static final int	STREAM_BEGIN_INDEX			= 7;
	public static final int	TEXT_BEGIN_INDEX				= 4;

	
	protected ByteBuf allocate(ByteBufferPool byteBufferPool) {
		return byteBufferPool.allocate(PROTOCOL_HADER);
	}

	protected IOReadFuture fetchFuture(Session session, ByteBuf buffer) throws IOException {
		
		
		byte _type = buffer.get(0);
		
		int type = (_type & 0xff) >> 6;
		
		return doDecode(session, buffer, type);
	}

	private IOReadFuture doDecode(Session session, ByteBuf buffer, int type) throws IOException {

		if (type == TYPE_TEXT) {
			return new TextReadFuture(session, buffer);
		} else if (type == TYPE_MULTI) {
			return new MultiReadFuture(session, buffer);
		} else if(type == TYPE_STREAM){
			return new StreamReadFuture(session, buffer);
		} else if(type == TYPE_BEAT){
			return new NIOBeatReadFuture(session);
		}else {
			throw new IOException("not happen");
		}
	}
}
