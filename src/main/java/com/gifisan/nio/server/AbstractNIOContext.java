package com.gifisan.nio.server;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.OutputStreamAcceptor;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.SessionFactory;
import com.gifisan.nio.component.UDPEndPointFactory;
import com.gifisan.nio.component.protocol.tcp.DefaultTCPProtocolDecoder;
import com.gifisan.nio.component.protocol.tcp.DefaultTCPProtocolEncoder;
import com.gifisan.nio.component.protocol.tcp.ProtocolDecoder;
import com.gifisan.nio.component.protocol.tcp.ProtocolEncoder;

public abstract class AbstractNIOContext extends AbstractLifeCycle implements NIOContext {

	private Map<String, Object>		attributes			= new HashMap<String, Object>();
	private UDPEndPointFactory		udpEndPointFactory		= new UDPEndPointFactory();
	protected Charset				encoding				= Encoding.DEFAULT;
	protected ProtocolEncoder		protocolEncoder		= new DefaultTCPProtocolEncoder();
	protected ProtocolDecoder		protocolDecoder		= new DefaultTCPProtocolDecoder();
	protected OutputStreamAcceptor	outputStreamAcceptor	= null;
	protected ReadFutureAcceptor		readFutureAcceptor		= null;
	protected SessionFactory		sessionFactory			= null;
	protected DatagramPacketAcceptor	datagramPacketAcceptor	= null;

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

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public DatagramPacketAcceptor getDatagramPacketAcceptor() {
		return datagramPacketAcceptor;
	}

	public UDPEndPointFactory getUDPEndPointFactory() {
		return udpEndPointFactory;
	}
	
	
}
