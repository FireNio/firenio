package com.gifisan.nio.server;

import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.Attributes;
import com.gifisan.nio.component.AttributesImpl;
import com.gifisan.nio.component.Connector;
import com.gifisan.nio.server.configuration.ServerConfiguration;

public final class NIOServer extends AbstractLifeCycle implements Attributes {

	private Attributes		attributes	= new AttributesImpl();
	private TCPConnector	tcpConnector	= null;
	private UDPConnector	udpConnector	= null;
	private ServerContext	context		= null;

	public NIOServer() {
		this.addLifeCycleListener(new NIOServerListener());
	}

	protected void doStart() throws Exception {

		this.context = new DefaultServerContext(this);
		
		this.context.start();
		
		ServerConfiguration configuration = context.getServerConfiguration();
		
		int SERVER_PORT = configuration.getSERVER_PORT();
		
		if (SERVER_PORT < 1) {
			throw new IllegalArgumentException("SERVER.PORT 参数错误");
		}

		this.tcpConnector = new TCPConnector(context, SERVER_PORT);
		
		this.tcpConnector.start();
		
		if (configuration.isSERVER_UDP_BOOT()) {
			
			this.udpConnector = new UDPConnector(context, SERVER_PORT+1);
			
			this.udpConnector.start();
		}
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(udpConnector);
		LifeCycleUtil.stop(tcpConnector);
		LifeCycleUtil.stop(context);
	}

	protected Connector getConnector() {
		return tcpConnector;
	}

	public Object removeAttribute(String key) {
		return this.attributes.removeAttribute(key);
	}

	public void setAttribute(String key, Object value) {
		this.attributes.setAttribute(key, value);
	}

	public Object getAttribute(String key) {
		return this.attributes.getAttribute(key);
	}

	public Set<String> getAttributeNames() {
		return this.attributes.getAttributeNames();
	}

	public void clearAttributes() {
		this.attributes.clearAttributes();

	}

}
