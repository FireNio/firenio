package com.gifisan.nio.extend.plugin.rtp.server;

import java.io.IOException;

import com.gifisan.nio.acceptor.ServerDPAcceptor;
import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.component.protocol.DatagramRequest;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.ApplicationContextUtil;
import com.gifisan.nio.extend.FixedSessionFactory;
import com.gifisan.nio.extend.LoginCenter;
import com.gifisan.nio.extend.security.AuthorityManager;

public class RTPServerDPAcceptor extends ServerDPAcceptor {
	
	public static final String BIND_SESSION = "BIND_SESSION";
	
	public static final String BIND_SESSION_CALLBACK = "BIND_SESSION_CALLBACK";
	
	public static final String SERVICE_NAME = RTPServerDPAcceptor.class.getSimpleName();
	
	private Logger logger = LoggerFactory.getLogger(RTPServerDPAcceptor.class);
	
	private RTPContext context = null;
	
	protected RTPServerDPAcceptor(RTPContext context) {
		this.context = context;
	}

	public void doAccept(UDPEndPoint endPoint, DatagramPacket packet,Session session) throws IOException {

		AuthorityManager authorityManager = ApplicationContextUtil.getAuthorityManager(session);
		
		if (authorityManager == null) {
			logger.debug("___________________null authority,packet:{}",packet);
			return;
		}
		
		if (!authorityManager.isInvokeApproved(getSERVICE_NAME())) {
			logger.debug("___________________not approved,packet:{}",packet);
			return;
		}
		
		RTPSessionAttachment attachment = (RTPSessionAttachment)session.getAttachment(context);
		
		RTPRoom room = attachment.getRtpRoom();
		
		if (room != null) {
			room.broadcast(endPoint, packet);
		}else{
			logger.debug("___________________null room,packet:{}",packet);
		}
	}
	
	protected void execute(UDPEndPoint endPoint,DatagramRequest request) {

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

	protected String getSERVICE_NAME() {
		return SERVICE_NAME;
	}
}
