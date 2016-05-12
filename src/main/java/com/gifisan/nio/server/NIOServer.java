package com.gifisan.nio.server;

import java.nio.charset.Charset;
import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.Attributes;
import com.gifisan.nio.component.AttributesImpl;
import com.gifisan.nio.component.Connector;

public final class NIOServer extends AbstractLifeCycle implements Attributes {

	private Attributes		attributes	= new AttributesImpl();
	private TCPConnector	tcpConnector	= null;
	private UDPConnector	udpConnector	= null;
	private ServerContext	context		= null;

	public NIOServer() {
		this.addLifeCycleListener(new NIOServerListener());
	}

	protected void doStart() throws Exception {

		SharedBundle bundle = SharedBundle.instance();

		int serverPort = bundle.getIntegerProperty("SERVER.PORT");

		if (serverPort < 1) {
			throw new IllegalArgumentException("SERVER.PORT 参数错误");
		}

		String encoding = bundle.getProperty("SERVER.ENCODING", "GBK");

		this.context = new ServerContextImpl(this);
		this.context.setServerPort(serverPort);
		this.context.setEncoding(Charset.forName(encoding));
		this.context.setServerCoreSize(bundle.getIntegerProperty("SERVER.CORE_SIZE", 4));

		this.tcpConnector = new TCPConnector(context, serverPort);
		
		this.context.start();
		
		this.tcpConnector.start();
		
		boolean UDP_BOOT = bundle.getBooleanProperty("SERVER.UDP_BOOT");
		
		if (UDP_BOOT) {
			
			this.udpConnector = new UDPConnector(context, serverPort+1);
			
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
