package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.common.DateUtil;

public class DefaultServerProtocolDecoder extends AbstractProtocolDecoder implements ProtocolDecoder {


	public boolean doDecodeExtend(EndPoint endPoint, ProtocolData data, Charset charset, byte type) throws IOException {

		if (type == 3) {

			System.out.println(">>read beat................." + DateUtil.now());

			((ServerProtocolData)data).setBeat(true);

			return true;
		}

		// HTTP REQUEST ?
		if (type == 71) {
			ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 105, 109, 32, 110, 111, 116, 32, 97, 110, 32, 104, 116, 116, 112, 32, 115,
					101, 114, 118, 101, 114, 32, 58, 41 });
			endPoint.completedWrite(buffer);
			return false;
		}

		return false;
	}

	public void gainNecessary(EndPoint endPoint, ProtocolData data, Charset charset, byte[] header) throws IOException {

		int serviceNameLength = header[1];

		ByteBuffer buffer = endPoint.completedRead(serviceNameLength);

		byte[] bytes = buffer.array();

		if (bytes == null || bytes.length == 0) {

			throw new IOException("service name is empty");
		}

		String serviceName = new String(bytes, 0, serviceNameLength);

		((ServerProtocolData)data).setServiceName(serviceName);
	}

}
