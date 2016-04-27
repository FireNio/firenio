package com.gifisan.nio.server;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.OutputStreamAcceptor;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.SessionFactory;

public abstract class AbstractNIOContext extends AbstractLifeCycle implements NIOContext {

	private Map<String, Object>		attributes			= new HashMap<String, Object>();
	protected Charset				encoding				= Encoding.DEFAULT;
	protected EndPointWriter		endPointWriter			= new EndPointWriter();
	protected OutputStreamAcceptor	outputStreamAcceptor	= null;
	protected ReadFutureAcceptor		readFutureAcceptor		= null;
	protected SessionFactory		sessionFactory			= null;

	public void clearAttributes() {
		this.attributes.clear();
	}

	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	public Set<String> getAttributeNames() {
		return this.attributes.keySet();
	}

	public Charset getEncoding() {
		return encoding;
	}

	public EndPointWriter getEndPointWriter() {
		return endPointWriter;
	}

	public OutputStreamAcceptor getOutputStreamAcceptor() {
		return outputStreamAcceptor;
	}

	public ReadFutureAcceptor getReadFutureAcceptor() {
		return readFutureAcceptor;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public Object removeAttribute(String key) {
		return this.attributes.remove(key);
	}

	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}
}
