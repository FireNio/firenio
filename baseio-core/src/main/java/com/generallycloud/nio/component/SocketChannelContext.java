package com.generallycloud.nio.component;

import com.generallycloud.nio.common.ssl.SslContext;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public interface SocketChannelContext extends ChannelContext {

	public abstract IoEventHandleAdaptor getIoEventHandleAdaptor();

	public abstract EventLoopGroup getEventLoopGroup();

	public abstract int getSessionAttachmentSize();
	
	public abstract void setSessionAttachmentSize(int sessionAttachmentSize);

	public abstract BeatFutureFactory getBeatFutureFactory();

	public abstract void setBeatFutureFactory(BeatFutureFactory beatFutureFactory);

	public abstract void setIoEventHandleAdaptor(IoEventHandleAdaptor ioEventHandleAdaptor);

	public abstract void setProtocolFactory(ProtocolFactory protocolFactory);
	
	public abstract ProtocolFactory getProtocolFactory();
	
	public abstract ProtocolEncoder getProtocolEncoder();
	
	public abstract SslContext getSslContext() ;

	public abstract void setSslContext(SslContext sslContext) ;
	
	public abstract ChannelByteBufReader getChannelByteBufReader();

	public abstract boolean isEnableSSL() ;
	
	public abstract SessionFactory getSessionFactory() ;

	public abstract void setSessionFactory(SessionFactory sessionFactory) ;

}