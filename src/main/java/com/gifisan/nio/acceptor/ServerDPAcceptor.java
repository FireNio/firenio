package com.gifisan.nio.acceptor;

import java.io.IOException;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.component.protocol.DatagramRequest;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.FixedSessionFactory;
import com.gifisan.nio.extend.LoginCenter;

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
		
		Session session = endPoint.getSession();
		
		if (session == null) {
			logger.debug("___________________null session,packet:{}",packet);
			return;
		}
		
		doAccept(endPoint, packet,session);
	}
	
	protected abstract void doAccept(UDPEndPoint endPoint, DatagramPacket packet,Session session) throws IOException ;

	protected abstract String getSERVICE_NAME();
	
	private void execute(UDPEndPoint endPoint,DatagramRequest request) {

		String serviceName = request.getServiceName();

		if (BIND_SESSION.equals(serviceName)) {
			
			Parameters parameters = request.getParameters();
			
			ApplicationContext context = ApplicationContext.getInstance();
			
			LoginCenter loginCenter = context.getLoginCenter();
			
			if (!loginCenter.isValidate(parameters)) {
				return;
			}
			
			FixedSessionFactory factory = context.getSessionFactory();
			
			String username = parameters.getParameter("username");
			
			Session session = factory.getSession(username);
			
			if (session == null) {
				return ;
			}
			
			endPoint.setSession(session);
			
			session.setUDPEndPoint(endPoint);
			
			ReadFuture future = ReadFutureFactory.create(session,BIND_SESSION_CALLBACK);
			
			logger.debug("___________________bind___session___{}",session);
			
			future.write(ByteUtil.TRUE);
			
			session.flush(future);
			
		}else{
			
			logger.debug(">>>> {}",request.getServiceName());
		}
	}

}
