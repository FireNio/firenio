/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.baseio.front;

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
