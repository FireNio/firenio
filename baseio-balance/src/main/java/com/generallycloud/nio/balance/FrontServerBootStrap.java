package com.generallycloud.nio.balance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.balance.router.FrontRouter;
import com.generallycloud.nio.balance.router.SimpleNextRouter;
import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SessionEventListener;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class FrontServerBootStrap {

	private ProtocolFactory			frontProtocolFactory;
	private ProtocolFactory			frontReverseProtocolFactory;
	private ServerConfiguration		frontServerConfiguration;
	private ServerConfiguration		frontReverseServerConfiguration;
	private List<SessionEventListener>	frontSessionEventListeners;
	private List<SessionEventListener>	frontReverseSessionEventListeners;
	private BeatFutureFactory		frontBeatFutureFactory;
	private BeatFutureFactory		frontReverseBeatFutureFactory;
	private FrontRouter				frontRouter;

	public void startup() throws IOException {

		FrontFacadeAcceptor frontFacadeAcceptor = new FrontFacadeAcceptor();
		
		if (frontRouter == null) {
			frontRouter = new SimpleNextRouter();
		}

		FrontContext frontContext = new FrontContext(frontFacadeAcceptor,frontRouter);

		BaseContext frontBaseContext = getFrontBaseContext(frontContext, frontServerConfiguration, frontProtocolFactory);

		BaseContext frontReverseBaseContext = getFrontReverseBaseContext(frontContext, frontReverseServerConfiguration,
				frontReverseProtocolFactory);

		frontFacadeAcceptor.start(frontContext, frontBaseContext, frontReverseBaseContext);
	}

	private BaseContext getFrontBaseContext(FrontContext frontContext, ServerConfiguration configuration,
			ProtocolFactory protocolFactory) {

		BaseContext context = new BaseContextImpl(configuration);

		context.setIOEventHandleAdaptor(frontContext.getFrontFacadeAcceptorHandler());

		context.addSessionEventListener(frontContext.getFrontFacadeAcceptorSEListener());

		context.setProtocolFactory(protocolFactory);

		context.setBeatFutureFactory(frontBeatFutureFactory);

		if (frontSessionEventListeners != null) {
			addSessionEventListener2Context(context, frontSessionEventListeners);
		}

		return context;
	}

	private BaseContext getFrontReverseBaseContext(FrontContext frontContext, ServerConfiguration configuration,
			ProtocolFactory protocolFactory) {

		BaseContext context = new BaseContextImpl(configuration);

		context.setIOEventHandleAdaptor(frontContext.getFrontReverseAcceptorHandler());

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

	public void addFrontSessionEventListener(SessionEventListener listener) {
		if (frontSessionEventListeners == null) {
			frontSessionEventListeners = new ArrayList<SessionEventListener>();
		}
		frontSessionEventListeners.add(listener);
	}

	public void addFrontReverseSessionEventListener(SessionEventListener listener) {
		if (frontReverseSessionEventListeners == null) {
			frontReverseSessionEventListeners = new ArrayList<SessionEventListener>();
		}
		frontReverseSessionEventListeners.add(listener);
	}

	private void addSessionEventListener2Context(BaseContext context, List<SessionEventListener> listeners) {
		for (SessionEventListener l : listeners) {
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
}
