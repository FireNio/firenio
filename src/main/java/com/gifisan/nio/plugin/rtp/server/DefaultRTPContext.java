package com.gifisan.nio.plugin.rtp.server;

import java.util.List;
import java.util.Map;

import com.gifisan.nio.component.AbstractPluginContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.server.DefaultServerContext;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.service.GenericServlet;
import com.gifisan.nio.server.service.NIOFilter;

public class DefaultRTPContext extends AbstractPluginContext implements RTPContext {
	
	private RTPRoomFactory rtpRoomFactory = new RTPRoomFactory();

	public void configFilter(List<NIOFilter> pluginFilters) {

	}

	public void configServlet(Map<String, GenericServlet> servlets) {

		servlets.put(RTPJoinRoomServlet.SERVICE_NAME, new RTPJoinRoomServlet());
		servlets.put(RTPCreateRoomServlet.SERVICE_NAME, new RTPCreateRoomServlet());
		servlets.put(RTPLeaveRoomServlet.SERVICE_NAME, new RTPLeaveRoomServlet());
		
	}

	public void initialize(ServerContext context, Configuration config) throws Exception {

		DefaultServerContext _context = (DefaultServerContext) context;

		_context.setDatagramPacketAcceptor(new RTPServerDPAcceptor(this));

		RTPContextFactory.initializeContext(this);

	}

	public RTPRoomFactory getRTPRoomFactory() {
		return rtpRoomFactory;
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {
		RTPContextFactory.setNullRTPContext();
	}
	
	
}
