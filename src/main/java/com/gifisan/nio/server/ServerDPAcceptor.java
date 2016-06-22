package com.gifisan.nio.server;

import java.io.IOException;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.component.protocol.DatagramRequest;
import com.gifisan.security.AuthorityManager;

public abstract class ServerDPAcceptor implements DatagramPacketAcceptor {
	
	public static final String BIND_SESSION = "BIND_SESSION";
	
	public static final String BIND_SESSION_CALLBACK = "BIND_SESSION_CALLBACK";
	
	private Logger logger = LoggerFactory.getLogger(ServerDPAcceptor.class);

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) throws IOException {

		NIOContext nioContext = endPoint.getContext();

		DatagramRequest request = DatagramRequest.create(packet, nioContext);

		if (request != null) {
			execute(endPoint,request);
			return;
		}
		
//		logger.debug("___________________server receive,packet:{}",packet);
		
		ServerSession session = (ServerSession)endPoint.getSession();
		
		if (session == null) {
			logger.debug("___________________null session,packet:{}",packet);
			return;
		}
		
		doAccept(endPoint, packet,session);
	}
	
	protected abstract void doAccept(UDPEndPoint endPoint, DatagramPacket packet,IOSession session) throws IOException ;

	protected abstract String getSERVICE_NAME();
	
	private void execute(UDPEndPoint endPoint,DatagramRequest request) {

		String serviceName = request.getServiceName();

		if (BIND_SESSION.equals(serviceName)) {
			
			Parameters parameters = request.getParameters();
			
			String sessionID = parameters.getParameter("sessionID");
			
			NIOContext context = endPoint.getContext();
			
			SessionFactory factory = context.getSessionFactory();
			
			ServerSession session = (ServerSession)factory.getIOSession(sessionID);
			
			if (session == null) {
				return ;
			}
			
			endPoint.setSession(session);
			
			session.setUDPEndPoint(endPoint);
			
			ReadFuture future = ReadFutureFactory.create(session, BIND_SESSION_CALLBACK);
			
			logger.debug("___________________bind___session___{}",session);
			
			future.write(ByteUtil.TRUE);
			
			session.flush(future);
			
		}else{
			
			logger.debug(">>>> {}",request.getServiceName());
		}
	}
	
	

}
