package com.generallycloud.nio.balance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.balance.router.FrontRouter;
import com.generallycloud.nio.balance.router.SimpleNextRouter;
import com.generallycloud.nio.common.ssl.SslContext;
import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.SocketSessionEventListener;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class FrontServerBootStrap {

	private ProtocolFactory				frontProtocolFactory;
	private ProtocolFactory				frontReverseProtocolFactory;
	private ServerConfiguration			frontServerConfiguration;
	private ServerConfiguration			frontReverseServerConfiguration;
	private List<SocketSessionEventListener>		frontSessionEventListeners;
	private List<SocketSessionEventListener>		frontReverseSessionEventListeners;
	private BeatFutureFactory			frontBeatFutureFactory;
	private BeatFutureFactory			frontReverseBeatFutureFactory;
	private ChannelLostReadFutureFactory	channelLostReadFutureFactory;
	private FrontRouter					frontRouter;
	private SslContext					sslContext;

	public void startup() throws IOException {

		FrontFacadeAcceptor frontFacadeAcceptor = new FrontFacadeAcceptor();

		if (frontRouter == null) {
			frontRouter = new SimpleNextRouter();
		}

		FrontContext frontContext = new FrontContext(frontFacadeAcceptor, frontRouter);

		SocketChannelContext frontBaseContext = getFrontBaseContext(frontContext, frontServerConfiguration,
				frontProtocolFactory);

		SocketChannelContext frontReverseBaseContext = getFrontReverseBaseContext(frontContext,
				frontReverseServerConfiguration, frontReverseProtocolFactory);
		
		frontContext.setChannelLostReadFutureFactory(channelLostReadFutureFactory);

		frontFacadeAcceptor.start(frontContext, frontBaseContext, frontReverseBaseContext);
	}

	private SocketChannelContext getFrontBaseContext(FrontContext frontContext, ServerConfiguration configuration,
			ProtocolFactory protocolFactory) {

		SocketChannelContext context = new SocketChannelContextImpl(configuration);

		context.setIoEventHandleAdaptor(frontContext.getFrontFacadeAcceptorHandler());

		context.addSessionEventListener(frontContext.getFrontFacadeAcceptorSEListener());

		context.setProtocolFactory(protocolFactory);

		context.setBeatFutureFactory(frontBeatFutureFactory);

		if (frontSessionEventListeners != null) {
			addSessionEventListener2Context(context, frontSessionEventListeners);
		}

		if (sslContext != null) {
			context.setSslContext(sslContext);
		}

		return context;
	}

	private SocketChannelContext getFrontReverseBaseContext(FrontContext frontContext, ServerConfiguration configuration,
			ProtocolFactory protocolFactory) {

		SocketChannelContext context = new SocketChannelContextImpl(configuration);

		context.setIoEventHandleAdaptor(frontContext.getFrontReverseAcceptorHandler());

		context.addSessionEventListener(frontContext.getFrontReverseAcceptorSEListener());

		context.setProtocolFactory(protocolFactory);

		context.setBeatFutureFactory(frontReverseBeatFutureFactory);

		if (frontReverseSessionEventListeners != null) {
			addSessionEventListener2Context(context, frontReverseSessionEventListeners);
		}

		return context;
	}

	public ProtocolFactory getFrontProtocolFactory() {
		return frontProtocolFactory;
	}

	public void setFrontProtocolFactory(ProtocolFactory frontProtocolFactory) {
		this.frontProtocolFactory = frontProtocolFactory;
	}

	public ProtocolFactory getFrontReverseProtocolFactory() {
		return frontReverseProtocolFactory;
	}

	public void setFrontReverseProtocolFactory(ProtocolFactory frontReverseProtocolFactory) {
		this.frontReverseProtocolFactory = frontReverseProtocolFactory;
	}

	public ServerConfiguration getFrontServerConfiguration() {
		return frontServerConfiguration;
	}

	public void setFrontServerConfiguration(ServerConfiguration frontServerConfiguration) {
		this.frontServerConfiguration = frontServerConfiguration;
	}

	public ServerConfiguration getFrontReverseServerConfiguration() {
		return frontReverseServerConfiguration;
	}

	public void setFrontReverseServerConfiguration(ServerConfiguration frontReverseServerConfiguration) {
		this.frontReverseServerConfiguration = frontReverseServerConfiguration;
	}

	public void addFrontSessionEventListener(SocketSessionEventListener listener) {
		if (frontSessionEventListeners == null) {
			frontSessionEventListeners = new ArrayList<SocketSessionEventListener>();
		}
		frontSessionEventListeners.add(listener);
	}

	public void addFrontReverseSessionEventListener(SocketSessionEventListener listener) {
		if (frontReverseSessionEventListeners == null) {
			frontReverseSessionEventListeners = new ArrayList<SocketSessionEventListener>();
		}
		frontReverseSessionEventListeners.add(listener);
	}

	private void addSessionEventListener2Context(SocketChannelContext context, List<SocketSessionEventListener> listeners) {
		for (SocketSessionEventListener l : listeners) {
			context.addSessionEventListener(l);
		}
	}

	public BeatFutureFactory getFrontBeatFutureFactory() {
		return frontBeatFutureFactory;
	}

	public BeatFutureFactory getFrontReverseBeatFutureFactory() {
		return frontReverseBeatFutureFactory;
	}

	public void setFrontBeatFutureFactory(BeatFutureFactory frontBeatFutureFactory) {
		this.frontBeatFutureFactory = frontBeatFutureFactory;
	}

	public void setFrontReverseBeatFutureFactory(BeatFutureFactory frontReverseBeatFutureFactory) {
		this.frontReverseBeatFutureFactory = frontReverseBeatFutureFactory;
	}

	public void setFrontRouter(FrontRouter frontRouter) {
		this.frontRouter = frontRouter;
	}

	public FrontRouter getFrontRouter() {
		return frontRouter;
	}

	public SslContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SslContext sslContext) {
		this.sslContext = sslContext;
	}

	public ChannelLostReadFutureFactory getChannelLostReadFutureFactory() {
		return channelLostReadFutureFactory;
	}

	public void setChannelLostReadFutureFactory(ChannelLostReadFutureFactory channelLostReadFutureFactory) {
		this.channelLostReadFutureFactory = channelLostReadFutureFactory;
	}
	
}
