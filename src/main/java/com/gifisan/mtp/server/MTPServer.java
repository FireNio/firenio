package com.gifisan.mtp.server;

import java.util.Set;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.component.AttributesImpl;

public final class MTPServer extends AbstractLifeCycle implements Attributes{
	
	public MTPServer() {
		this.contextFactory = new ServletContextFactory(this);
		this.addLifeCycleListener(new MTPServerListener());
	}

	private Attributes attributes = new AttributesImpl() ;
	
	private Connector connector = new NIOConnector();
	
	private ServletContextFactory contextFactory = null;
	
	protected void doStart() throws Exception {
		this.contextFactory.start();
		this.connector.setServer(this);
		this.connector.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(connector);
		LifeCycleUtil.stop(contextFactory);
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
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
	
	public void setPort(int port){
		this.connector.setPort(port);
	}
	
}
