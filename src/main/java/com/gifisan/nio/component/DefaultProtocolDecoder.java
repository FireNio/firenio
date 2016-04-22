package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.protocol.Decoder;

public class DefaultProtocolDecoder implements ProtocolDecoder {

	protected Decoder[]	decoders	= new Decoder[4];

	public DefaultProtocolDecoder(Decoder textDecoder, Decoder multiDecoder) {
		this.decoders[0] = textDecoder;
		this.decoders[1] = multiDecoder;
	}

	public IOReadFuture decode(EndPoint endPoint) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(1);

		int length = endPoint.read(buffer);

		if (length < 1) {
			if (length < 0) {
				endPoint.endConnect();
			}
			return null;
		}

		byte type = buffer.get(0);

		if (type < 2) {

			if (type < 0) {
				return null;
			}

			return this.doDecode(endPoint, type);

		} else {

			return this.doDecodeExtend(endPoint, type);
		}
	}

	public IOReadFuture doDecodeExtend(EndPoint endPoint, byte type) throws IOException {

		return null;
	}

	private IOReadFuture doDecode(EndPoint endPoint, byte type) throws IOException {

		byte[] header = readHeader(endPoint);

		if (header == null) {
			endPoint.endConnect();
			return null;
		}

		return decoders[type].decode(endPoint, header);

	}

	private byte[] readHeader(EndPoint endPoint) throws IOException {

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
