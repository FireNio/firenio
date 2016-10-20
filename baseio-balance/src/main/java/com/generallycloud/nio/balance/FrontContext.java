package com.generallycloud.nio.balance;

import com.generallycloud.nio.balance.router.FrontRouter;

public class FrontContext {

	public static final String			FRONT_CHANNEL_LOST		= "FRONT_CHANNEL_LOST";
	public static final String			FRONT_RECEIVE_BROADCAST	= "FRONT_RECEIVE_BROADCAST";

	private FrontFacadeAcceptor			frontFacadeAcceptor;
	private FrontReverseAcceptor			frontReverseAcceptor;
	private FrontFacadeAcceptorSEListener	frontFacadeAcceptorSEListener;
	private FrontReverseAcceptorSEListener	frontReverseAcceptorSEListener;
	private FrontRouter					frontRouter;
	private FrontReverseAcceptorHandler	frontReverseAcceptorHandler;
	private FrontFacadeAcceptorHandler		frontFacadeAcceptorHandler;
	private ChannelLostReadFutureFactory	channelLostReadFutureFactory;

	protected FrontContext(FrontFacadeAcceptor facadeAcceptor,FrontRouter frontRouter) {
		this.frontFacadeAcceptor = facadeAcceptor;
		this.frontRouter = frontRouter;
		this.frontFacadeAcceptorSEListener = new FrontFacadeAcceptorSEListener(this);
		this.frontReverseAcceptorSEListener = new FrontReverseAcceptorSEListener(this);
		this.frontFacadeAcceptorHandler = new FrontFacadeAcceptorHandler(this);
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

	public FrontRouter getFrontRouter() {
		return frontRouter;
	}

	public ChannelLostReadFutureFactory getChannelLostReadFutureFactory() {
		return channelLostReadFutureFactory;
	}

	public void setChannelLostReadFutureFactory(ChannelLostReadFutureFactory channelLostReadFutureFactory) {
		this.channelLostReadFutureFactory = channelLostReadFutureFactory;
	}

}
