package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public abstract class AbstractProtocolDecoder implements ProtocolDecoder {

	public boolean decode(EndPoint endPoint, ProtocolData data, Charset charset) throws IOException {

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

			return this.doDecode(endPoint, data, charset, type);

		} else {

			return this.doDecodeExtend(endPoint, data, charset, type);
		}
	}

	public boolean doDecodeExtend(EndPoint endPoint, ProtocolData data, Charset charset, byte type) throws IOException {

		return true;
	}

	public void decodeText(EndPoint endPoint, ProtocolData data, Charset charset, byte[] header) throws IOException {

		int contentLength = getTextLength(header);

		String text = readText(contentLength, charset, endPoint);

		data.setText(text);
	}

	public void decodeStream(EndPoint endPoint, ProtocolData data, Charset charset, byte[] header) throws IOException {

		if (endPoint.sessionSize() > 1) {
			throw new IOException("unique session can be created when trans strean data");
		}

		int streamLength = getStreamLength(header);

		InputStream inputStream = readInputStream(streamLength, endPoint);

		data.setInputStream(inputStream);
		
		endPoint.setInputStream(inputStream);
	}

	public void decodeMult(EndPoint endPoint, ProtocolData data, Charset charset, byte[] header) throws IOException {
		if (endPoint.sessionSize() > 1) {
			throw new IOException("unique session can be created when trans strean data");
		}

		int contentLength = getTextLength(header);

		int streamLength = getStreamLength(header);

		String text = readText(contentLength, charset, endPoint);

		InputStream inputStream = readInputStream(streamLength, endPoint);

		data.setText(text);

		data.setInputStream(inputStream);
		
		endPoint.setInputStream(inputStream);
	}

	protected boolean doDecode(EndPoint endPoint, ProtocolData data, Charset charset, byte type) throws IOException {

		byte[] header = readHeader(endPoint);

		if (header == null) {
			return false;
		}
		
		data.setProtocolType(type);

		this.gainSessionID(endPoint, data, charset, header);

		this.gainNecessary(endPoint, data, charset, header);

		return headerDecoders[type].decode(this, endPoint, data, charset, header);

	}

	protected void gainSessionID(EndPoint endPoint, ProtocolData data, Charset charset, byte[] header)
			throws IOException {

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

	protected int getStreamLength(byte[] header) {
		int v0 = (header[5] & 0xff);
		int v1 = (header[6] & 0xff) << 8;
		int v2 = (header[7] & 0xff) << 16;
		int v3 = (header[8] & 0xff) << 24;
		return v0 | v1 | v2 | v3;
	}

	protected String readText(int length, Charset charset, EndPoint endPoint) throws IOException {

		if (length < 1) {

			return null;
		}

		ByteBuffer buffer = endPoint.read(length);

		byte[] bytes = buffer.array();

		String content = new String(bytes, charset);

		return content;
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

	protected InputStream readInputStream(int length, EndPoint endPoint) throws IOException {

		return length == 0 ? null : new EndPointInputStream(endPoint, length);
	}

	private static abstract class Decoder {

		abstract boolean decode(ProtocolDecoder decoder, EndPoint endPoint, ProtocolData data, Charset charset,
				byte[] header) throws IOException;

	}

	private static final Decoder[]	headerDecoders	= new Decoder[] {
											// TEXT
			new Decoder() {
				public boolean decode(ProtocolDecoder decoder, EndPoint endPoint, ProtocolData data,
						Charset charset, byte[] header) throws IOException {

					decoder.decodeText(endPoint, data, charset, header);

					return true;

				}
			},
			// STREAM
			new Decoder() {
				public boolean decode(ProtocolDecoder decoder, EndPoint endPoint, ProtocolData data,
						Charset charset, byte[] header) throws IOException {

					decoder.decodeStream(endPoint, data, charset, header);

					return true;

				}
			},
			// MULT
			new Decoder() {
				public boolean decode(ProtocolDecoder decoder, EndPoint endPoint, ProtocolData data,
						Charset charset, byte[] header) throws IOException {

					decoder.decodeMult(endPoint, data, charset, header);

					return true;

				}
			},
			// BEAT
			new Decoder() {
				public boolean decode(ProtocolDecoder decoder, EndPoint endPoint, ProtocolData data,
						Charset charset, byte[] header) throws IOException {
					return true;
				}
			}						
	};
}
