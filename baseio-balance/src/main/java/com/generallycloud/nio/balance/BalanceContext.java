package com.generallycloud.nio.balance;

import com.generallycloud.nio.balance.router.BalanceRouter;

public class BalanceContext {

	public static final String				BALANCE_CHANNEL_LOST		= "BALANCE_CHANNEL_LOST";
	public static final String				BALANCE_RECEIVE_BROADCAST	= "BALANCE_RECEIVE_BROADCAST";

	private BalanceFacadeAcceptor				balanceFacadeAcceptor;
	private BalanceReverseAcceptor			balanceReverseAcceptor;
	private BalanceFacadeAcceptorSEListener		balanceFacadeAcceptorSEListener;
	private BalanceReverseAcceptorSEListener	balanceReverseAcceptorSEListener;
	private BalanceRouter					balanceRouter;
	private BalanceReverseAcceptorHandler		balanceReverseAcceptorHandler;
	private BalanceFacadeAcceptorHandler		balanceFacadeAcceptorHandler;
	private ChannelLostReadFutureFactory		channelLostReadFutureFactory;
	private FacadeInterceptor				facadeInterceptor;

	protected BalanceContext(BalanceFacadeAcceptor facadeAcceptor, BalanceRouter balanceRouter) {
		this.balanceFacadeAcceptor = facadeAcceptor;
		this.balanceRouter = balanceRouter;
		this.facadeInterceptor = new FacadeInterceptorImpl();
		this.balanceReverseAcceptor = new BalanceReverseAcceptor();
		this.balanceFacadeAcceptorSEListener = new BalanceFacadeAcceptorSEListener(this);
		this.balanceReverseAcceptorSEListener = new BalanceReverseAcceptorSEListener(this);
		this.balanceFacadeAcceptorHandler = new BalanceFacadeAcceptorHandler(this);
		this.balanceReverseAcceptorHandler = new BalanceReverseAcceptorHandler(this);
	}

	public BalanceFacadeAcceptor getBalanceFacadeAcceptor() {
		return balanceFacadeAcceptor;
	}

	public BalanceFacadeAcceptorHandler getBalanceFacadeAcceptorHandler() {
		return balanceFacadeAcceptorHandler;
	}

	public BalanceFacadeAcceptorSEListener getBalanceFacadeAcceptorSEListener() {
		return balanceFacadeAcceptorSEListener;
	}

	public BalanceReverseAcceptor getBalanceReverseAcceptor() {
		return balanceReverseAcceptor;
	}

	public BalanceReverseAcceptorHandler getBalanceReverseAcceptorHandler() {
		return balanceReverseAcceptorHandler;
	}

	public BalanceReverseAcceptorSEListener getBalanceReverseAcceptorSEListener() {
		return balanceReverseAcceptorSEListener;
	}

	public BalanceRouter getBalanceRouter() {
		return balanceRouter;
	}

	public ChannelLostReadFutureFactory getChannelLostReadFutureFactory() {
		return channelLostReadFutureFactory;
	}

	public void setChannelLostReadFutureFactory(ChannelLostReadFutureFactory channelLostReadFutureFactory) {
		this.channelLostReadFutureFactory = channelLostReadFutureFactory;
	}

	public FacadeInterceptor getFacadeInterceptor() {
		return facadeInterceptor;
	}

	public void setFacadeInterceptor(FacadeInterceptor facadeInterceptor) {
		if (facadeInterceptor == null) {
			throw new IllegalArgumentException("null facadeInterceptor");
		}
		this.facadeInterceptor = facadeInterceptor;
	}
	
}
