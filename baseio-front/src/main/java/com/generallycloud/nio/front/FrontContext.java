package com.generallycloud.nio.front;

public class FrontContext {

	public static final String				BALANCE_CHANNEL_LOST		= "BALANCE_CHANNEL_LOST";
	public static final String				BALANCE_RECEIVE_BROADCAST	= "BALANCE_RECEIVE_BROADCAST";

	private BalanceFacadeConnector			balanceFacadeConnector;
	private BalanceFacadeConnectorHandler		balanceFacadeConnectorHandler;
	private BalanceFacadeConnectorSEListener	balanceFacadeConnectorSEListener;
	private ChannelLostReadFutureFactory		channelLostReadFutureFactory;
	private FrontFacadeAcceptor				frontFacadeAcceptor;
	private FrontFacadeAcceptorHandler			frontFacadeAcceptorHandler;
	private FrontFacadeAcceptorSEListener		frontFacadeAcceptorSEListener;
	private FrontInterceptor					frontInterceptor;
	private FrontRouter						frontRouter;

	protected FrontContext() {
		this.frontRouter = new FrontRouter();
		this.balanceFacadeConnector = new BalanceFacadeConnector();
		this.frontFacadeAcceptor = new FrontFacadeAcceptor();
		this.frontFacadeAcceptorSEListener = new FrontFacadeAcceptorSEListener(this);
		this.balanceFacadeConnectorSEListener = new BalanceFacadeConnectorSEListener();
		this.frontFacadeAcceptorHandler = new FrontFacadeAcceptorHandler(this);
		this.balanceFacadeConnectorHandler = new BalanceFacadeConnectorHandler(this);
	}

	public BalanceFacadeConnector getBalanceFacadeConnector() {
		return balanceFacadeConnector;
	}

	public BalanceFacadeConnectorHandler getBalanceFacadeConnectorHandler() {
		return balanceFacadeConnectorHandler;
	}

	public BalanceFacadeConnectorSEListener getBalanceFacadeConnectorSEListener() {
		return balanceFacadeConnectorSEListener;
	}

	public ChannelLostReadFutureFactory getChannelLostReadFutureFactory() {
		return channelLostReadFutureFactory;
	}

	public FrontFacadeAcceptor getFrontFacadeAcceptor() {
		return frontFacadeAcceptor;
	}

	public FrontFacadeAcceptorHandler getFrontFacadeAcceptorHandler() {
		return frontFacadeAcceptorHandler;
	}

	public FrontFacadeAcceptorSEListener getFrontFacadeAcceptorSEListener() {
		return frontFacadeAcceptorSEListener;
	}

	public FrontInterceptor getFrontInterceptor() {
		return frontInterceptor;
	}

	public FrontRouter getFrontRouter() {
		return frontRouter;
	}

	public void setChannelLostReadFutureFactory(ChannelLostReadFutureFactory channelLostReadFutureFactory) {
		this.channelLostReadFutureFactory = channelLostReadFutureFactory;
	}

	public void setFrontInterceptor(FrontInterceptor frontInterceptor) {
		if (frontInterceptor == null) {
			throw new IllegalArgumentException("null frontInterceptor");
		}
		this.frontInterceptor = frontInterceptor;
	}

}
