package com.generallycloud.nio.extend.plugin.rtp.server;

import java.util.Map;

import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.AbstractPluginContext;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.configuration.Configuration;
import com.generallycloud.nio.extend.service.FutureAcceptorService;

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
		return (RTPSessionAttachment) session.getAttachment(this.getPluginIndex());
	}

	public RTPRoomFactory getRTPRoomFactory() {
		return rtpRoomFactory;
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		instance = null;
	}

}
