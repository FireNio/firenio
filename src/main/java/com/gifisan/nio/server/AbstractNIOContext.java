package com.gifisan.nio.server;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.SessionFactory;
import com.gifisan.nio.server.selector.SelectionAcceptor;

public abstract class AbstractNIOContext extends AbstractLifeCycle implements NIOContext {

	private EndPointWriter		endPointWriter		= new EndPointWriter();
	protected Charset			encoding			= Encoding.DEFAULT;
	protected SelectionAcceptor	selectionAcceptor	= null;
	protected ReadFutureAcceptor	readFutureAcceptor	= null;
	protected SessionFactory	sessionFactory		= null;

	public Charset getEncoding() {
		return encoding;
	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}

	public EndPointWriter getEndPointWriter() {
		return endPointWriter;
	}

	public SelectionAcceptor getSelectionAcceptor() {
		return selectionAcceptor;
	}

	public ReadFutureAcceptor getReadFutureAcceptor() {
		return readFutureAcceptor;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	private Map<String, Object>	attributes	= new HashMap<String, Object>();

	public Object removeAttribute(String key) {
		return this.attributes.remove(key);
	}

	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	public Set<String> getAttributeNames() {
		return this.attributes.keySet();
	}

	public void clearAttributes() {
		this.attributes.clear();
	}

	protected void doStart() throws Exception {
		this.endPointWriter.start();

	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(endPointWriter);
	}

}
