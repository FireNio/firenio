package com.gifisan.nio.plugin.rtp.server;

import java.util.Map;

import com.gifisan.nio.component.AbstractPluginContext;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.service.GenericReadFutureAcceptor;

public class DefaultRTPContext extends AbstractPluginContext implements RTPContext {

	private RTPRoomFactory	rtpRoomFactory	= new RTPRoomFactory();

	public void configFutureAcceptor(Map<String, GenericReadFutureAcceptor> acceptors) {

		acceptors.put(RTPJoinRoomServlet.SERVICE_NAME, new RTPJoinRoomServlet());
		acceptors.put(RTPCreateRoomServlet.SERVICE_NAME, new RTPCreateRoomServlet());
		acceptors.put(RTPLeaveRoomServlet.SERVICE_NAME, new RTPLeaveRoomServlet());
	}
	
	

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		NIOContext nioContext = context.getContext();

		nioContext.setDatagramPacketAcceptor(new RTPServerDPAcceptor(this));

		RTPContextFactory.initializeContext(this);

	}

	public RTPRoomFactory getRTPRoomFactory() {
		return rtpRoomFactory;
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		RTPContextFactory.setNullRTPContext();
	}

}
