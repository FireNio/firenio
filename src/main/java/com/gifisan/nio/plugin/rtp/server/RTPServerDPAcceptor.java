package com.gifisan.nio.plugin.rtp.server;

import java.io.IOException;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.component.protocol.DatagramRequest;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.ReadFutureFactory;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServerSession;
import com.gifisan.nio.server.SessionFactory;
import com.gifisan.security.AuthorityManager;

public class RTPServerDPAcceptor implements DatagramPacketAcceptor {
	
	public static final String BIND_SESSION = "BIND_SESSION";
	
	public static final String BIND_SESSION_CALLBACK = "BIND_SESSION_CALLBACK";
	
	public static final String SERVICE_NAME = RTPServerDPAcceptor.class.getSimpleName();
	
	private Logger logger = LoggerFactory.getLogger(RTPServerDPAcceptor.class);

	private RTPContext context = null;
	
	protected RTPServerDPAcceptor(RTPContext context) {
		this.context = context;
	}

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) throws IOException {

		NIOContext nioContext = endPoint.getContext();

		DatagramRequest request = DatagramRequest.create(packet, nioContext);

		if (request != null) {
			execute(endPoint,request);
			return;
		}
		
		ServerSession session = (ServerSession) endPoint.getTCPSession();
		
		if (session == null) {
			return;
		}
		
		AuthorityManager authorityManager = session.getAuthorityManager();
		
		if (authorityManager == null) {
			
			return;
		}
		
		if (!authorityManager.isInvokeApproved(SERVICE_NAME)) {
			
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
			
			ServerContext context = (ServerContext) endPoint.getContext();
			
			SessionFactory factory = context.getSessionFactory();
			
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
			
			logger.debug(">>>> {}",request.getServiceName());
		}
	}

}
