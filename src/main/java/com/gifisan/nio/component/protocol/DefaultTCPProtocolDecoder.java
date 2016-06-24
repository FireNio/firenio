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

		ByteBuffer header = ByteBuffer.allocate(PROTOCOL_HADER);

		int length = endPoint.read(header);

		if (length < 1) {
			if (length < 0) {
				endPoint.endConnect();
			}
			return null;
		}

		byte type = header.get(0);

		if (type < 3) {

			if (type < 0) {
				return null;
			}

			return this.doDecode(endPoint, header, type);

		} else {

			return this.doDecodeExtend(endPoint, header, type);
		}
	}

	private IOReadFuture doDecode(TCPEndPoint endPoint, ByteBuffer header, byte type) throws IOException {

		if (type == TYPE_TEXT) {
			return new TextReadFuture(endPoint.getSession(), header);
		} else if (type == TYPE_MULTI) {
			return new MultiReadFuture(endPoint.getSession(), header);
		} else {
			return new StreamReadFuture(endPoint.getSession(), header);
		}
	}

	public IOReadFuture doDecodeExtend(TCPEndPoint endPoint, ByteBuffer header, byte type) throws IOException {

		return null;
	}

}
