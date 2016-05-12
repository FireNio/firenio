package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.protocol.udp.DatagramPacket;
import com.gifisan.nio.component.protocol.udp.DatagramRequest;
import com.gifisan.nio.server.NIOContext;

public class ServerDatagramPacketAcceptor implements DatagramPacketAcceptor {

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) throws IOException {

		NIOContext context = endPoint.getContext();

		DatagramRequest request = DatagramRequest.create(packet, context);

		if (request != null) {
			execute(endPoint,request);
			return;
		}
		
		UDPEndPointFactory factory = context.getUDPEndPointFactory();

		UDPEndPoint targetEndPoint = factory.getUDPEndPoint(packet.getTargetEndpoint());
		
		if (targetEndPoint == null) {
			return;
		}

		targetEndPoint.write(ByteBuffer.wrap(packet.getData()));

	}

	private void execute(UDPEndPoint endPoint,DatagramRequest request) {

		String serviceName = request.getServiceName();

		if ("BIND_SESSION".equals(serviceName)) {

			// FIXME BIND SESSION
		}
	}

}
