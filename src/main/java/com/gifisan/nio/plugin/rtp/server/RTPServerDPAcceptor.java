package com.gifisan.nio.plugin.rtp.server;

import java.io.IOException;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.ManagedIOSessionFactory;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.component.protocol.DatagramRequest;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.ReadFutureFactory;
import com.gifisan.nio.server.ServerSession;

public class RTPServerDPAcceptor implements DatagramPacketAcceptor {
	
	private RTPContext context = RTPContextFactory.getRTPContext();
	
	public static final String BIND_SESSION = "BIND_SESSION";
	
	public static final String BIND_SESSION_CALLBACK = "BIND_SESSION_CALLBACK";

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) throws IOException {

		NIOContext nioContext = endPoint.getContext();

		DatagramRequest request = DatagramRequest.create(packet, nioContext);

		if (request != null) {
			execute(endPoint,request);
			return;
		}
		
		IOSession session = (IOSession) endPoint.getTCPSession();
		
		if (session == null) {
			return;
		}
		
		if (!context.isLogined(session)) {
			return;
		}

		RTPSessionAttachment attachment = (RTPSessionAttachment)session.getAttachment(context);
		
		RTPRoom room = attachment.getRtpRoom();
		
		if (room != null) {
			room.broadcast(endPoint, packet);
		}
	}

	private void execute(UDPEndPoint endPoint,DatagramRequest request) {

		String serviceName = request.getServiceName();

		if (BIND_SESSION.equals(serviceName)) {
			
			Parameters parameters = request.getParameters();
			
			String sessionID = parameters.getParameter("sessionID");
			
			NIOContext context = endPoint.getContext();
			
			ManagedIOSessionFactory factory = context.getManagedIOSessionFactory();
			
			ServerSession session = (ServerSession)factory.getIOSession(sessionID);
			
			if (session == null) {
				return ;
			}
			
			endPoint.setTCPSession(session);
			
			session.setUDPEndPoint(endPoint);
			
			ServerReadFuture future = ReadFutureFactory.create(session, BIND_SESSION_CALLBACK);
			
			future.write(ByteUtil.TRUE);
			
			session.flush(future);
			
		}else{
			
			DebugUtil.debug(">>>>"+request.getServiceName());
		}
	}

}
