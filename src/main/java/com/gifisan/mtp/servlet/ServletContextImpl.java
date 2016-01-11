package com.gifisan.mtp.servlet;

import java.nio.charset.Charset;
import java.util.Set;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.component.AttributesImpl;
import com.gifisan.mtp.server.MTPServer;
import com.gifisan.mtp.server.ServletContext;
import com.gifisan.mtp.server.session.MTPSessionFactory;

public class ServletContextImpl extends AbstractLifeCycle implements ServletContext {

	private AttributesImpl		attributesImpl	= new AttributesImpl();
	private Charset			encoding		= Encoding.DEFAULT;
	private MTPServer			server		= null;
	private MTPSessionFactory	sessionFactory	= null;

	public ServletContextImpl(MTPServer server) {
		this.server = server;
		this.sessionFactory = new MTPSessionFactory(this);
	}

	public void clearAttributes() {
		attributesImpl.clearAttributes();
	}

	protected void doStart() throws Exception {
		sessionFactory.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(sessionFactory);
	}

	public Object getAttribute(String key) {
		return attributesImpl.getAttribute(key);
	}

	public Set<String> getAttributeNames() {
		return attributesImpl.getAttributeNames();
	}

	public Charset getEncoding() {
		return encoding;
	}

	public MTPSessionFactory getMTPSessionFactory() {
		return sessionFactory;
	}

	public MTPServer getServer() {
		return server;
	}

	public String getWebAppLocalAddress() {
		// TODO dongze 获取webapp本地部署路径
		String path = System.getProperty("user.dir");
		return path;
	}

	public void removeAttribute(String key) {
		attributesImpl.removeAttribute(key);

	}

	public void setAttribute(String key, Object value) {
		attributesImpl.setAttribute(key, value);

	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}

}
