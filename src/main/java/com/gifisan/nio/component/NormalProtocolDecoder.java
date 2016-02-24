package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.DateUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServerEndPoint;

public class NormalProtocolDecoder implements ProtocolDecoder {

	private InputStream	inputStream	= null;
	private String		content		= null;
	private String		serviceName	= null;
	private boolean	beat			= false;
	private byte		sessionID		= 0;

	public InputStream getInputStream() {
		return inputStream;
	}

	public String getContent() {
		return content;
	}

	public String getServiceName() {
		return serviceName;
	}

	public byte getSessionID() {
		return sessionID;
	}

	private void reset() {
		this.beat = false;
		this.content = null;
		this.inputStream = null;
	}

	public boolean decode(ServerEndPoint endPoint) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		int length = endPoint.read(buffer);

		// if (length == -1) {
		// return false;
		// }
		//
		// if (length == 0) {
		// return true;
		// }

		if (length < 1) {
			return false;
		}

		byte type = buffer.get(0);

		if (type < 3) {

			if (type < 0) {
				return false;
			}

			this.reset();

			buffer = ByteBuffer.allocate(9);
			length = endPoint.read(buffer);

			byte[] header = buffer.array();

			if (length < 9) {
				// 如果一次读取不到9个byte
				// 这样的连接持续下去也是无法进行业务操作
				// 还有一种情况是有人在恶意攻击服务器
				return false;
			}

			return headerDecoders[type].decode(endPoint.getContext(), this, endPoint, header);
		} else {
			if (type == 3) {
				System.out.println(">>read beat................." + DateUtil.now());
				this.beat = true;
				return true;
			}

			// HTTP REQUEST ?
			if (type == 71) {
				endPoint.write(new byte[] { 105, 109, 32, 110, 111, 116, 32, 97, 110, 32, 104, 116, 116, 112, 32,
						115, 101, 114, 118, 101, 114, 32, 58, 41 });
				return false;
			}

			return false;
		}

	}

	public boolean isBeat() {
		return this.beat;
	}

	private static abstract class Decoder {

		int getContentLength(byte[] header) {
			int v0 = (header[2] & 0xff);
			int v1 = (header[3] & 0xff) << 8;
			int v2 = (header[4] & 0xff) << 16;
			return v0 | v1 | v2;
		}

		String readContent(int contentLength, ServerContext context, ServerEndPoint endPoint) throws IOException {
			if (contentLength > 0) {
				ByteBuffer buffer = endPoint.read(contentLength);
				byte[] bytes = buffer.array();
				String content = new String(bytes, context.getEncoding());
				return content;
			}
			return null;
		}

		int getStreamLength(byte[] header) {
			int v0 = (header[5] & 0xff);
			int v1 = (header[6] & 0xff) << 8;
			int v2 = (header[7] & 0xff) << 16;
			int v3 = (header[8] & 0xff) << 24;
			return v0 | v1 | v2 | v3;
		}

		InputStream readInputStream(int streamLength, ServerEndPoint endPoint) throws IOException {
			if (streamLength == 0) {
				return null;
			}

			InputStream inputStream = new EndPointInputStream(endPoint, streamLength);
			endPoint.setInputStream(inputStream);
			return inputStream;
		}

		void gainNecessary(NormalProtocolDecoder decoder, ServerEndPoint endPoint, byte[] header) throws IOException {
			int kLength = header[1];
			ByteBuffer buffer = endPoint.read(kLength);
			byte[] bytes = buffer.array();
			byte sessionID = header[0];
			String serviceName = new String(bytes, 0, kLength);

			if (StringUtil.isNullOrBlank(serviceName)) {
				endPoint.endConnect();
				throw new IOException("service name is empty");
			}

			if (sessionID > 3 || sessionID < 0) {
				throw new IOException("invalidate session id");
			}

			decoder.sessionID = sessionID;
			decoder.serviceName = serviceName;
		}

		abstract boolean decode(ServerContext context, NormalProtocolDecoder decoder, ServerEndPoint endPoint,
				byte[] header) throws IOException;

	}

	private static final Decoder[]	headerDecoders	= new Decoder[] {
											// TEXT
			new Decoder() {
				public boolean decode(ServerContext context, NormalProtocolDecoder decoder,
						ServerEndPoint endPoint, byte[] header) throws IOException {

					gainNecessary(decoder, endPoint, header);

					int contentLength = getContentLength(header);

					decoder.content = readContent(contentLength, context, endPoint);

					return true;

				}
			},
			// STREAM
			new Decoder() {
				public boolean decode(ServerContext context, NormalProtocolDecoder decoder,
						ServerEndPoint endPoint, byte[] header) throws IOException {

					gainNecessary(decoder, endPoint, header);

					if (endPoint.sessionSize() > 1) {
						throw new IOException("unique session can be created when trans strean data");
					}

					int streamLength = getStreamLength(header);

					decoder.inputStream = readInputStream(streamLength, endPoint);

					return true;

				}
			},
			// MULT
			new Decoder() {
				public boolean decode(ServerContext context, NormalProtocolDecoder decoder,
						ServerEndPoint endPoint, byte[] header) throws IOException {

					gainNecessary(decoder, endPoint, header);

					if (endPoint.sessionSize() > 1) {
						throw new IOException("unique session can be created when trans strean data");
					}

					int contentLength = getContentLength(header);
					int streamLength = getStreamLength(header);

					decoder.content = readContent(contentLength, context, endPoint);

					decoder.inputStream = readInputStream(streamLength, endPoint);

					return true;

				}
			},
			// BEAT
			new Decoder() {
				public boolean decode(ServerContext context, NormalProtocolDecoder decoder,
						ServerEndPoint endPoint, byte[] header) throws IOException {
					return true;

				}
			}

											};

}
