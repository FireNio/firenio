package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.TCPEndPoint;
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
	
	
	private IOReadFuture doDecode(TCPEndPoint endPoint, byte type) throws IOException {

		if (type == TYPE_TEXT) {
			return new TextReadFuture(endPoint, endPoint.getSession());
		}else if(type == TYPE_MULTI){
			return new MultiReadFuture(endPoint, endPoint.getSession());
		}else{
			return new StreamReadFuture(endPoint, endPoint.getSession());
		}
	}
	
	public IOReadFuture doDecodeExtend(TCPEndPoint endPoint, byte type) throws IOException {

		return null;
	}

}
