package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.future.IOReadFuture;

public class DefaultProtocolDecoder implements ProtocolDecoder {

	protected Decoder[]	decoders	= new Decoder[3];

	public DefaultProtocolDecoder(Charset charset) {
		this.decoders[TEXT] 	= new TextDecoder(charset);
		this.decoders[STREAM] 	= new StreamDecoder(charset);
		this.decoders[MULTI] 	= new MultiDecoder(charset);
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

		if (type < 3) {

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
