package com.gifisan.nio.plugin.rtp.server;

import java.util.Map;

import com.gifisan.nio.component.AbstractPluginContext;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.service.FutureAcceptorService;

public class RTPContext extends AbstractPluginContext {

	private RTPRoomFactory		rtpRoomFactory	= new RTPRoomFactory();
	private static RTPContext	instance		= null;

	public static RTPContext getInstance() {
		return instance;
	}

	public void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors) {

		acceptors.put(RTPJoinRoomServlet.SERVICE_NAME, new RTPJoinRoomServlet());
		acceptors.put(RTPCreateRoomServlet.SERVICE_NAME, new RTPCreateRoomServlet());
		acceptors.put(RTPLeaveRoomServlet.SERVICE_NAME, new RTPLeaveRoomServlet());
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		NIOContext nioContext = context.getContext();

		nioContext.setDatagramPacketAcceptor(new RTPServerDPAcceptor(this));

		context.addSessionEventListener(new RTPSessionEventListener());
		
		instance = this;
	}
	
	public RTPSessionAttachment getSessionAttachment(Session session){
		return (RTPSessionAttachment) session.getAttachment(this);
	}

	public RTPRoomFactory getRTPRoomFactory() {
		return rtpRoomFactory;
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		instance = null;
	}

}
