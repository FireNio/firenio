package com.generallycloud.nio.extend.plugin.rtp.server;

import java.io.IOException;

import com.generallycloud.nio.acceptor.ServerDPAcceptor;
import com.generallycloud.nio.codec.nio.future.NIOReadFutureImpl;
import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.ApplicationContextUtil;
import com.generallycloud.nio.extend.FixedSessionFactory;
import com.generallycloud.nio.extend.LoginCenter;
import com.generallycloud.nio.extend.security.AuthorityManager;
import com.generallycloud.nio.protocol.DatagramPacket;
import com.generallycloud.nio.protocol.DatagramRequest;
import com.generallycloud.nio.protocol.ReadFuture;

public class RTPServerDPAcceptor extends ServerDPAcceptor {
	
	public static final String BIND_SESSION = "BIND_SESSION";
	
	public static final String BIND_SESSION_CALLBACK = "BIND_SESSION_CALLBACK";
	
	public static final String SERVICE_NAME = RTPServerDPAcceptor.class.getSimpleName();
	
	private Logger logger = LoggerFactory.getLogger(RTPServerDPAcceptor.class);
	
	private RTPContext context = null;
	
	protected RTPServerDPAcceptor(RTPContext context) {
		this.context = context;
	}

	public void doAccept(DatagramChannel channel, DatagramPacket packet,Session session) throws IOException {

		AuthorityManager authorityManager = ApplicationContextUtil.getAuthorityManager(session);
		
		if (authorityManager == null) {
			logger.debug("___________________null authority,packet:{}",packet);
			return;
		}
		
		if (!authorityManager.isInvokeApproved(getSERVICE_NAME())) {
			logger.debug("___________________not approved,packet:{}",packet);
			return;
		}
		
		RTPSessionAttachment attachment = (RTPSessionAttachment)session.getAttachment(context.getPluginIndex());
		
		RTPRoom room = attachment.getRtpRoom();
		
		if (room != null) {
			room.broadcast(channel, packet);
		}else{
			logger.debug("___________________null room,packet:{}",packet);
		}
	}
	
	protected void execute(DatagramChannel channel,DatagramRequest request) {

		String serviceName = request.getFutureName();

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
			
			channel.setSession(session);
			
			session.setDatagramChannel(channel);
			
			ReadFuture future = new NIOReadFutureImpl(BIND_SESSION_CALLBACK);
			
			future.setIOEventHandle(session.getContext().getIOEventHandleAdaptor());
			
			logger.debug("___________________bind___session___{}",session);
			
			future.write(ByteUtil.TRUE);
			
			try {
				session.flush(future);
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
			
		}else{
			logger.debug(">>>> {}",request.getFutureName());
		}
	}

	protected String getSERVICE_NAME() {
		return SERVICE_NAME;
	}
}
