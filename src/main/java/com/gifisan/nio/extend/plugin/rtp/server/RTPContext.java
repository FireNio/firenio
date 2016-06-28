package com.gifisan.nio.extend.plugin.rtp.server;

import java.util.Map;

import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.extend.AbstractPluginContext;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.configuration.Configuration;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public class RTPContext extends AbstractPluginContext {

	private RTPRoomFactory		rtpRoomFactory	= new RTPRoomFactory();
	private static RTPContext	instance		;

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
