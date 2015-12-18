package com.gifisan.mtp.servlet;

import java.util.Set;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.component.AttributesImpl;
import com.gifisan.mtp.server.MTPServer;
import com.gifisan.mtp.server.context.ServletContext;
import com.gifisan.mtp.server.session.MTPSessionFactory;

public class ServletContextImpl extends AbstractLifeCycle implements ServletContext {

	private MTPSessionFactory sessionFactory = new MTPSessionFactory();
	
	public ServletContextImpl(MTPServer server) {
		this.server = server;
	}

	protected void doStart() throws Exception {
		sessionFactory.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(sessionFactory);
	}

	private String encoding = "UTF-8";

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	private MTPServer server = null;

	public MTPServer getServer() {
		return server;
	}
	
	

	public MTPSessionFactory getMTPSessionFactory() {
		return sessionFactory;
	}

	public String getWebAppLocalAddress() {
		// TODO dongze  获取webapp本地部署路径
		String path = System.getProperty("user.dir");
		return path;
	}
	
	private AttributesImpl attributesImpl = new AttributesImpl();

	public void removeAttribute(String key) {
		attributesImpl.removeAttribute(key);
		
	}

	public void setAttribute(String key, Object value) {
		attributesImpl.setAttribute(key, value);
		
	}

	public Object getAttribute(String key) {
		return attributesImpl.getAttribute(key);
	}

	public Set<String> getAttributeNames() {
		return attributesImpl.getAttributeNames();
	}

	public void clearAttributes() {
		attributesImpl.clearAttributes();
	}
	

	
	
}
