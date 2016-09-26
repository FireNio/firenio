package com.generallycloud.nio.component;

import java.nio.charset.Charset;

import com.generallycloud.nio.LifeCycle;
import com.generallycloud.nio.acceptor.DatagramChannelFactory;
import com.generallycloud.nio.buffer.ByteBufferPool;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.component.protocol.ProtocolFactory;
import com.generallycloud.nio.configuration.ServerConfiguration;

public interface NIOContext extends Attributes, LifeCycle {

	public abstract Charset getEncoding();

	public abstract IOEventHandleAdaptor getIOEventHandleAdaptor();

	public abstract IOReadFutureAcceptor getIOReadFutureAcceptor();

	public abstract ServerConfiguration getServerConfiguration();

	public abstract SessionFactory getSessionFactory();

	public abstract IOService getTCPService();

	public abstract void setTCPService(IOService tcpService);

	public abstract IOService getUDPService();

	public abstract void setUDPService(IOService udpService);

	public abstract EventLoopGroup getEventLoopGroup();

	public abstract Sequence getSequence();

	public abstract long getSessionIdleTime();
	
	public abstract int getSessionAttachmentSize();
	
	public abstract void setSessionAttachmentSize(int sessionAttachmentSize);

	public abstract BeatFutureFactory getBeatFutureFactory();

	public abstract void setBeatFutureFactory(BeatFutureFactory beatFutureFactory);

	public abstract long getStartupTime();

	public abstract DatagramChannelFactory getDatagramChannelFactory();

	public abstract void setIOEventHandleAdaptor(IOEventHandleAdaptor ioEventHandleAdaptor);

	public abstract void setProtocolFactory(ProtocolFactory protocolFactory);

	public abstract void setDatagramChannelFactory(DatagramChannelFactory datagramChannelFactory);

	public abstract DatagramPacketAcceptor getDatagramPacketAcceptor();

	public abstract void setSessionFactory(SessionFactory sessionFactory);

	public abstract void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor);

	public abstract SessionEventListenerWrapper getSessionEventListenerStub();
	
	public abstract ProtocolFactory getProtocolFactory();
	
	public abstract ByteBufferPool getHeapByteBufferPool();
	
//	public abstract ByteBufferPool getDirectByteBufferPool();

	public abstract void addSessionEventListener(SessionEventListener listener);

}