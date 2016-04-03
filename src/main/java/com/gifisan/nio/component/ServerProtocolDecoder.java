package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.common.DateUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.protocol.Decoder;

public class ServerProtocolDecoder extends AbstractProtocolDecoder implements ProtocolDecoder {

	private Logger			logger	= LoggerFactory.getLogger(ServerProtocolDecoder.class);
	private StringBuilder	http		= new StringBuilder();
	private byte[]		httpArray	= null;

	public ServerProtocolDecoder(Decoder textDecoder,Decoder streamDecoder,Decoder multDecoder) {
		super(textDecoder, streamDecoder, multDecoder);

		http.append("HTTP/1.1 200 OK\n");
		http.append("Server: nimbleio/1.1\n");
		http.append("Content-Type:text/html;charset=GBK\n\n");
		
		http.append("<!DOCTYPE html>");
		http.append("<html lang=\"en\">");
		http.append("	<head>");
		http.append("		<meta name =\"viewport\" content =\"initial-scale=1, maximum-scale=3, minimum-scale=1, user-scalable=no\">");
		http.append("		<title>nimbleio</title>");
		http.append("	</head>");
		http.append("<body>");
		http.append("	<p style=\"color:#FDA58C;\">");
		http.append("		im not an http server :)  ");
		http.append("		<a style=\"color:#F94F4F;\" href=\"https://github.com/NimbleIO/NimbleIO#readme\" >fork me@https://github.com/nimbleio/nimbleio</a>");
		http.append("	</p>");
		http.append("</body>");
		http.append("</html>");

		httpArray = http.toString().getBytes();
	}

	public boolean doDecodeExtend(EndPoint endPoint, ProtocolDataImpl data, byte type) throws IOException {
		if (type == 3) {

			DebugUtil.debug(">>read beat................." + DateUtil.now());

			((ServerProtocolData) data).setBeat(true);

			return true;
		}

		// HTTP REQUEST ?
		if (type == 71) {
			ByteBuffer buffer = ByteBuffer.wrap(httpArray);
			endPoint.write(buffer);
			endPoint.endConnect();
			logger.info("来自[ {}:{} ]的HTTP请求", endPoint.getRemoteAddr(),endPoint.getRemotePort());
			return false;
		}else{
			endPoint.endConnect();
			return false;
		}
	}

	public void gainNecessary(EndPoint endPoint, ProtocolDataImpl data, byte[] header) throws IOException {

		int serviceNameLength = header[1];

		ByteBuffer buffer = endPoint.read(serviceNameLength);

		byte[] bytes = buffer.array();

		if (bytes == null || bytes.length == 0) {

			throw new IOException("service name is empty");
		}

		String serviceName = new String(bytes, 0, serviceNameLength);

		((ServerProtocolData) data).setServiceName(serviceName);
	}

}
