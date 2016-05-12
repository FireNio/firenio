package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.component.protocol.udp.DatagramPacket;
import com.gifisan.nio.component.protocol.udp.DatagramRequest;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.ReadFutureFactory;

public class ServerDatagramPacketAcceptor implements DatagramPacketAcceptor {

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) throws IOException {

		NIOContext context = endPoint.getContext();

		DatagramRequest request = DatagramRequest.create(packet, context);

		if (request != null) {
			execute(endPoint,request);
			return;
		}
		
		UDPEndPointFactory factory = context.getUDPEndPointFactory();

		UDPEndPoint targetEndPoint = factory.getUDPEndPoint(packet.getTargetEndpointID());
		
		if (targetEndPoint == null) {
			return;
		}

		
		
		targetEndPoint.sendPacket(ByteBuffer.wrap(packet.getData()));

	}

	private void execute(UDPEndPoint endPoint,DatagramRequest request) {

		String serviceName = request.getServiceName();

		if ("BIND_SESSION".equals(serviceName)) {
			
			Parameters parameters = request.getParameters();
			
			String sessionID = parameters.getParameter("sessionID");
			
			NIOContext context = endPoint.getContext();
			
			ManagedIOSessionFactory factory = context.getManagedIOSessionFactory();
			
			IOSession session = factory.getIOSession(sessionID);
			
			if (session == null) {
				return ;
			}
			
			endPoint.setTCPSession(session);
			
			ServerReadFuture future = ReadFutureFactory.create(session, "BIND_SESSION_CALLBACK");
			
			future.write("SUCCESS");
			
			session.flush(future);
			
		}else{
			
			DebugUtil.debug(">>>>"+request.getServiceName());
		}
	}

}
