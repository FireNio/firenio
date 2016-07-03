package com.gifisan.nio.front;

public class FrontContext {

	public static final String			FRONT_CHANNEL_LOST			= "FRONT_CHANNEL_LOST";
	public static final String			FRONT_RECEIVE_BROADCAST		= "FRONT_RECEIVE_BROADCAST";

	private FrontFacadeAcceptor			frontFacadeAcceptor;
	private FrontReverseAcceptor			frontReverseAcceptor;
	private FrontConfiguration			frontConfiguration;
	private FrontFacadeAcceptorSEListener	frontFacadeAcceptorSEListener;
	private FrontReverseAcceptorSEListener	frontReverseAcceptorSEListener;
	private FrontRouterMapping			frontRouterMapping			= new FrontRouterMapping();
	private FrontReverseAcceptorHandler	frontReverseAcceptorHandler	;
	private FrontFacadeAcceptorHandler		frontFacadeAcceptorHandler	;

	public FrontContext(FrontConfiguration configuration) {
		this.frontConfiguration = configuration;
		this.frontFacadeAcceptorSEListener = new FrontFacadeAcceptorSEListener(frontRouterMapping);
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

	public void setFrontFacadeAcceptor(FrontFacadeAcceptor frontFacadeAcceptor) {
		this.frontFacadeAcceptor = frontFacadeAcceptor;
	}

	public void setFrontFacadeAcceptorHandler(FrontFacadeAcceptorHandler frontFacadeAcceptorHandler) {
		this.frontFacadeAcceptorHandler = frontFacadeAcceptorHandler;
	}

	public void setFrontReverseAcceptor(FrontReverseAcceptor frontReverseAcceptor) {
		this.frontReverseAcceptor = frontReverseAcceptor;
	}

	public void setFrontReverseAcceptorHandler(FrontReverseAcceptorHandler frontReverseAcceptorHandler) {
		this.frontReverseAcceptorHandler = frontReverseAcceptorHandler;
	}

	public FrontConfiguration getFrontConfiguration() {
		return frontConfiguration;
	}

}
