package com.gifisan.nio.server;

import java.nio.charset.Charset;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.component.DefaultProtocolEncoder;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.ProtocolDecoder;
import com.gifisan.nio.component.ProtocolEncoder;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.ServerProtocolDecoder;
import com.gifisan.nio.component.SessionFactory;
import com.gifisan.nio.component.protocol.ServerMultiDecoder;
import com.gifisan.nio.component.protocol.MultiDecoder;
import com.gifisan.nio.component.protocol.TextDecoder;
import com.gifisan.nio.server.selector.SelectionAcceptor;

public abstract class AbstractNIOContext extends AbstractLifeCycle implements NIOContext {

	private Charset			encoding			= null;
	private ProtocolDecoder		protocolDecoder	= null;
	private ProtocolEncoder		protocolEncoder	= new DefaultProtocolEncoder();
	protected EndPointWriter	endPointWriter		= new EndPointWriter();
	protected SelectionAcceptor	selectionAcceptor	= null;
	protected ReadFutureAcceptor	readFutureAcceptor	= null;
	protected SessionFactory sessionFactory = null;
	
	
	public AbstractNIOContext() {
		this.protocolDecoder = new ServerProtocolDecoder(
				new TextDecoder(encoding),
				new MultiDecoder(encoding),
				new ServerMultiDecoder(encoding));
	}

	public Charset getEncoding() {
		return encoding;
	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
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
	

}
