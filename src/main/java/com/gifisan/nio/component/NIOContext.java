package com.gifisan.nio.component;

import java.nio.charset.Charset;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.acceptor.UDPEndPointFactory;
import com.gifisan.nio.component.concurrent.ThreadPool;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public interface NIOContext extends Attributes, LifeCycle {

	public abstract Charset getEncoding();

	public abstract IOEventHandleAdaptor getIOEventHandleAdaptor();

	public abstract ProtocolDecoder getProtocolDecoder();

	public abstract ProtocolEncoder getProtocolEncoder();

	public abstract ReadFutureAcceptor getReadFutureAcceptor();

	public abstract ServerConfiguration getServerConfiguration();

	public abstract SessionFactory getSessionFactory();

	public abstract IOService getTCPService();

	public abstract void setTCPService(IOService tcpService);

	public abstract IOService getUDPService();

	public abstract void setUDPService(IOService udpService);

	public abstract ThreadPool getThreadPool();

	public abstract UDPEndPointFactory getUDPEndPointFactory();

	public abstract void setIOEventHandleAdaptor(IOEventHandleAdaptor ioEventHandleAdaptor);

	public abstract void setProtocolDecoder(ProtocolDecoder protocolDecoder);

	public abstract void setProtocolEncoder(ProtocolEncoder protocolEncoder);

	public abstract void setServerConfiguration(ServerConfiguration serverConfiguration);

	public abstract void setUDPEndPointFactory(UDPEndPointFactory udpEndPointFactory);

	public abstract DatagramPacketAcceptor getDatagramPacketAcceptor();
	
	public abstract void setSessionFactory(SessionFactory sessionFactory);

	public abstract void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor);

	public abstract SessionEventListenerWrapper getSessionEventListenerStub();

	public abstract void addSessionEventListener(SessionEventListener listener);

}