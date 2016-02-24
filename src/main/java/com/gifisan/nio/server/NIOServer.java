package com.gifisan.nio.server;

import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.AttributesImpl;

public final class NIOServer extends AbstractLifeCycle implements Attributes {

	public NIOServer(int port) {
		this.context = new ServerContextImpl(this);
		this.connector = new NIOConnector(context);
		this.connector.setPort(port);
		this.addLifeCycleListener(new NIOServerListener());
	}

	private Attributes		attributes	= new AttributesImpl();
	private Connector		connector		= null;
	private ServerContext	context		= null;

	protected void doStart() throws Exception {
		this.context.start();
		this.connector.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(connector);
		LifeCycleUtil.stop(context);
	}

	protected Connector getConnector() {
		return connector;
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
