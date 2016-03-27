package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.component.protocol.Decoder;
import com.gifisan.nio.component.protocol.MultDecoder;
import com.gifisan.nio.component.protocol.StreamDecoder;
import com.gifisan.nio.component.protocol.TextDecoder;

public abstract class AbstractProtocolDecoder implements ProtocolDecoder {

	protected Decoder[]	decoders	= new Decoder[4];

	public AbstractProtocolDecoder(Charset charset) {
		this.decoders[0] = new TextDecoder(charset);
		this.decoders[1] = new StreamDecoder(charset);
		this.decoders[2] = new MultDecoder(charset);
	}

	public boolean decode(EndPoint endPoint, ProtocolDataImpl data) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(1);

		int length = endPoint.read(buffer);

		if (length < 1) {
			if (length < 0) {
				endPoint.endConnect();
			}
			return false;
		}

		byte type = buffer.get(0);

		data.setProtocolType(type);

		if (type < 3) {

			if (type < 0) {
				return false;
			}

			return this.doDecode(endPoint, data, type);

		} else {

			return this.doDecodeExtend(endPoint, data, type);
		}
	}

	public boolean doDecodeExtend(EndPoint endPoint, ProtocolDataImpl data, byte type) throws IOException {

		return true;
	}

	protected boolean doDecode(EndPoint endPoint, ProtocolDataImpl data, byte type) throws IOException {

		byte[] header = readHeader(endPoint);

		if (header == null) {
			return false;
		}

		Decoder decoder = decoders[type];

		data.setProtocolType(type);

		data.setHeader(header);

		data.setDecoder(decoder);

		this.gainSessionID(endPoint, data, header);

		this.gainNecessary(endPoint, data, header);

		return decodeTextBuffer(decoder, endPoint, data, header);
	}

	protected boolean decodeTextBuffer(Decoder decoder, EndPoint endPoint, ProtocolDataImpl data, byte[] header)
			throws IOException {

		int textLength = getTextLength(header);

		if (textLength == 0) {
			decoder.decode(endPoint, data, header, null);

			return true;
		}

		ByteBuffer buffer = ByteBuffer.allocate(textLength);

		if (decoder.progressRead(endPoint, buffer)) {

			decoder.decode(endPoint, data, header, buffer);

			return true;
		} else {
			endPoint.setSchedule(new SlowlyNetworkReader(decoder, data, buffer));

			return false;
		}
	}

	protected void gainSessionID(EndPoint endPoint, ProtocolDataImpl data, byte[] header) throws IOException {

		byte sessionID = header[0];

		if (sessionID > 3 || sessionID < 0) {
			throw new IOException("invalidate session id");
		}

		data.setSessionID(sessionID);

	}

	protected int getTextLength(byte[] header) {
		int v0 = (header[2] & 0xff);
		int v1 = (header[3] & 0xff) << 8;
		int v2 = (header[4] & 0xff) << 16;
		return v0 | v1 | v2;
	}

	protected byte[] readHeader(EndPoint endPoint) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(9);

		int length = endPoint.read(buffer);

		if (length < 9) {
			// 如果一次读取不到9个byte
			// 这样的连接持续下去也是无法进行业务操作
			// 还有一种情况是有人在恶意攻击服务器

			return null;
		}

		return buffer.array();
	}

}
