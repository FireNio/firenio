package com.gifisan.mtp.server;

import java.util.Set;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.component.AttributesImpl;
import com.gifisan.mtp.servlet.ServletContextImpl;

public final class MTPServer extends AbstractLifeCycle implements Attributes {

	public MTPServer(int port) {
		this.context = new ServletContextImpl(this);
		this.connector = new NIOConnector(context);
		this.connector.setPort(port);
		this.addLifeCycleListener(new MTPServerListener());
	}

	private Attributes		attributes	= new AttributesImpl();
	private Connector		connector		= null;
	private ServletContext	context		= null;

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

	public void removeAttribute(String key) {
		this.attributes.removeAttribute(key);
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
