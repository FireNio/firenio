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

public class NIOProtocolDecoder implements ProtocolDecoder {

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
