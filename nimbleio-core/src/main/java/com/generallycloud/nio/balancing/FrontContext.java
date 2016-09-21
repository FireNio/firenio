package com.generallycloud.nio.balancing;


public class FrontContext {

	public static final String			FRONT_CHANNEL_LOST			= "FRONT_CHANNEL_LOST";
	public static final String			FRONT_RECEIVE_BROADCAST		= "FRONT_RECEIVE_BROADCAST";

	private FrontFacadeAcceptor			frontFacadeAcceptor;
	private FrontReverseAcceptor			frontReverseAcceptor;
	private FrontFacadeAcceptorSEListener	frontFacadeAcceptorSEListener;
	private FrontReverseAcceptorSEListener	frontReverseAcceptorSEListener;
	private FrontRouterMapping			frontRouterMapping			= new FrontRouterMapping();
	private FrontReverseAcceptorHandler	frontReverseAcceptorHandler	;
	private FrontFacadeAcceptorHandler		frontFacadeAcceptorHandler	;
	private ChannelLostReadFutureFactory	channelLostReadFutureFactory	;
	
	

	protected FrontContext(FrontFacadeAcceptor facadeAcceptor) {
		this.frontFacadeAcceptor = facadeAcceptor;
		this.frontFacadeAcceptorSEListener = new FrontFacadeAcceptorSEListener(this);
		this.frontReverseAcceptorSEListener = new FrontReverseAcceptorSEListener(frontRouterMapping);
		this.frontFacadeAcceptorHandler = new FrontFacadeAcceptorHandler(frontRouterMapping);
		this.frontReverseAcceptorHandler = new FrontReverseAcceptorHandler(this);
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

	public FrontReverseAcceptor getFrontReverseAcceptor() {
		return frontReverseAcceptor;
	}

	public FrontReverseAcceptorHandler getFrontReverseAcceptorHandler() {
		return frontReverseAcceptorHandler;
	}

	public FrontReverseAcceptorSEListener getFrontReverseAcceptorSEListener() {
		return frontReverseAcceptorSEListener;
	}

	public FrontRouterMapping getFrontRouterMapping() {
		return frontRouterMapping;
	}

	public ChannelLostReadFutureFactory getChannelLostReadFutureFactory() {
		return channelLostReadFutureFactory;
	}

	public void setChannelLostReadFutureFactory(ChannelLostReadFutureFactory channelLostReadFutureFactory) {
		this.channelLostReadFutureFactory = channelLostReadFutureFactory;
	}

}
