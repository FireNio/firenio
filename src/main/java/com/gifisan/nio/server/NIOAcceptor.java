package com.gifisan.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.protocol.DefaultTCPProtocolDecoder;
import com.gifisan.nio.component.protocol.DefaultTCPProtocolEncoder;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.server.configuration.ServerConfiguration;

public final class NIOAcceptor extends AbstractLifeCycle{

	private TCPAcceptor		tcpAcceptor		= null;
	private UDPAcceptor		udpAcceptor		= null;
	private NIOContext		context			= null;
	private IOEventHandle	ioEventHandle		= null;
	private ProtocolEncoder	protocolEncoder	= null;
	private ProtocolDecoder	protocolDecoder	= null;

	public NIOAcceptor(IOEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
		this.addLifeCycleListener(new NIOServerListener());
	}

	protected void doStart() throws Exception {
		if (protocolEncoder == null) {
			protocolEncoder = new DefaultTCPProtocolEncoder();
		}

		if (protocolDecoder == null) {
			protocolDecoder = new DefaultTCPProtocolDecoder();
		}

		this.context = new DefaultNIOContext(protocolDecoder, protocolEncoder, ioEventHandle);

		try {
			this.context.start();
		} catch (Exception e) {
			throw new IOException(e);
		}

		ServerConfiguration configuration = context.getServerConfiguration();

		int SERVER_PORT = configuration.getSERVER_PORT();

		if (SERVER_PORT < 1) {
			throw new IllegalArgumentException("SERVER.PORT 参数错误");
		}

		this.tcpAcceptor = new TCPAcceptor(context);

		this.tcpAcceptor.bind(getInetSocketAddress(SERVER_PORT));

		if (configuration.isSERVER_UDP_BOOT()) {

			this.udpAcceptor = new UDPAcceptor(context);

			this.udpAcceptor.bind(getInetSocketAddress(SERVER_PORT + 1));
		}
		
	}

	protected void doStop() throws Exception {
		udpAcceptor.unbind();
		tcpAcceptor.unbind();
		LifeCycleUtil.stop(context);
	}

	private InetSocketAddress getInetSocketAddress(int port) {
		return new InetSocketAddress(port);
	}

	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		this.protocolEncoder = protocolEncoder;
	}

	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		this.protocolDecoder = protocolDecoder;
	}

	public NIOContext getContext() {
		return context;
	}
	
	
}
