package com.generallycloud.nio.balance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.balance.router.BalanceRouter;
import com.generallycloud.nio.balance.router.SimpleNextRouter;
import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.SocketSessionEventListener;
import com.generallycloud.nio.component.ssl.SslContext;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class BalanceServerBootStrap {

	private ProtocolFactory					balanceProtocolFactory;
	private ProtocolFactory					balanceReverseProtocolFactory;
	private ServerConfiguration				balanceServerConfiguration;
	private ServerConfiguration				balanceReverseServerConfiguration;
	private List<SocketSessionEventListener>	balanceSessionEventListeners;
	private List<SocketSessionEventListener>	balanceReverseSessionEventListeners;
	private BeatFutureFactory				balanceBeatFutureFactory;
	private BeatFutureFactory				balanceReverseBeatFutureFactory;
	private ChannelLostReadFutureFactory		channelLostReadFutureFactory;
	private BalanceRouter					balanceRouter;
	private SslContext						sslContext;

	public void startup() throws IOException {

		if (balanceRouter == null) {
			balanceRouter = new SimpleNextRouter();
		}

		BalanceContext balanceContext = new BalanceContext(balanceRouter);
		
		BalanceFacadeAcceptor balanceFacadeAcceptor = balanceContext.getBalanceFacadeAcceptor();

		SocketChannelContext balanceChannelContext = getBalanceChannelContext(balanceContext, balanceServerConfiguration,
				balanceProtocolFactory);
		
		balanceChannelContext.setSocketSessionFactory(new BalanceFacadeSocketSessionFactory());

		SocketChannelContext balanceReverseChannelContext = getBalanceReverseChannelContext(balanceContext,
				balanceReverseServerConfiguration, balanceReverseProtocolFactory);
		
		balanceReverseChannelContext.setSocketSessionFactory(new BalanceReverseSocketSessionFactory());

		balanceContext.setChannelLostReadFutureFactory(channelLostReadFutureFactory);

		balanceFacadeAcceptor.start(balanceContext, balanceChannelContext, balanceReverseChannelContext);
	}

	private SocketChannelContext getBalanceChannelContext(BalanceContext balanceContext,
			ServerConfiguration configuration, ProtocolFactory protocolFactory) {

		SocketChannelContext context = new SocketChannelContextImpl(configuration);

		context.setIoEventHandleAdaptor(balanceContext.getBalanceFacadeAcceptorHandler());

		context.addSessionEventListener(balanceContext.getBalanceFacadeAcceptorSEListener());

		context.setProtocolFactory(protocolFactory);

		context.setBeatFutureFactory(balanceBeatFutureFactory);

		if (balanceSessionEventListeners != null) {
			addSessionEventListener2Context(context, balanceSessionEventListeners);
		}

		if (sslContext != null) {
			context.setSslContext(sslContext);
		}

		return context;
	}

	private SocketChannelContext getBalanceReverseChannelContext(BalanceContext balanceContext,
			ServerConfiguration configuration, ProtocolFactory protocolFactory) {

		SocketChannelContext context = new SocketChannelContextImpl(configuration);

		context.setIoEventHandleAdaptor(balanceContext.getBalanceReverseAcceptorHandler());

		context.addSessionEventListener(balanceContext.getBalanceReverseAcceptorSEListener());

		context.setProtocolFactory(protocolFactory);

		context.setBeatFutureFactory(balanceReverseBeatFutureFactory);

		if (balanceReverseSessionEventListeners != null) {
			addSessionEventListener2Context(context, balanceReverseSessionEventListeners);
		}

		return context;
	}

	public ProtocolFactory getBalanceProtocolFactory() {
		return balanceProtocolFactory;
	}

	public void setBalanceProtocolFactory(ProtocolFactory balanceProtocolFactory) {
		this.balanceProtocolFactory = balanceProtocolFactory;
	}

	public ProtocolFactory getBalanceReverseProtocolFactory() {
		return balanceReverseProtocolFactory;
	}

	public void setBalanceReverseProtocolFactory(ProtocolFactory balanceReverseProtocolFactory) {
		this.balanceReverseProtocolFactory = balanceReverseProtocolFactory;
	}

	public ServerConfiguration getBalanceServerConfiguration() {
		return balanceServerConfiguration;
	}

	public void setBalanceServerConfiguration(ServerConfiguration balanceServerConfiguration) {
		this.balanceServerConfiguration = balanceServerConfiguration;
	}

	public ServerConfiguration getBalanceReverseServerConfiguration() {
		return balanceReverseServerConfiguration;
	}

	public void setBalanceReverseServerConfiguration(ServerConfiguration balanceReverseServerConfiguration) {
		this.balanceReverseServerConfiguration = balanceReverseServerConfiguration;
	}

	public void addBalanceSessionEventListener(SocketSessionEventListener listener) {
		if (balanceSessionEventListeners == null) {
			balanceSessionEventListeners = new ArrayList<SocketSessionEventListener>();
		}
		balanceSessionEventListeners.add(listener);
	}

	public void addBalanceReverseSessionEventListener(SocketSessionEventListener listener) {
		if (balanceReverseSessionEventListeners == null) {
			balanceReverseSessionEventListeners = new ArrayList<SocketSessionEventListener>();
		}
		balanceReverseSessionEventListeners.add(listener);
	}

	private void addSessionEventListener2Context(SocketChannelContext context,
			List<SocketSessionEventListener> listeners) {
		for (SocketSessionEventListener l : listeners) {
			context.addSessionEventListener(l);
		}
	}

	public BeatFutureFactory getBalanceBeatFutureFactory() {
		return balanceBeatFutureFactory;
	}

	public BeatFutureFactory getBalanceReverseBeatFutureFactory() {
		return balanceReverseBeatFutureFactory;
	}

	public void setBalanceBeatFutureFactory(BeatFutureFactory balanceBeatFutureFactory) {
		this.balanceBeatFutureFactory = balanceBeatFutureFactory;
	}

	public void setBalanceReverseBeatFutureFactory(BeatFutureFactory balanceReverseBeatFutureFactory) {
		this.balanceReverseBeatFutureFactory = balanceReverseBeatFutureFactory;
	}

	public void setBalanceRouter(BalanceRouter balanceRouter) {
		this.balanceRouter = balanceRouter;
	}

	public BalanceRouter getBalanceRouter() {
		return balanceRouter;
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
