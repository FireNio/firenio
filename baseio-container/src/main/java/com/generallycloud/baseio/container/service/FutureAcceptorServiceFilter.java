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
package com.generallycloud.baseio.container.service;

import java.io.IOException;

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.DynamicClassLoader;
import com.generallycloud.baseio.container.RESMessage;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.live.LifeCycleUtil;
import com.generallycloud.baseio.protocol.NamedReadFuture;
import com.generallycloud.baseio.protocol.ReadFuture;

public class FutureAcceptorServiceFilter extends FutureAcceptorFilter {

	private Logger						logger	= LoggerFactory.getLogger(FutureAcceptorServiceFilter.class);
	private DynamicClassLoader			classLoader;
	private FutureAcceptorServiceLoader	acceptorServiceLoader;

	public FutureAcceptorServiceFilter() {
		this.setSortIndex(Integer.MAX_VALUE);
	}

	@Override
	protected void accept(SocketSession session, NamedReadFuture future) throws Exception {

		String serviceName = future.getFutureName();

		if (StringUtil.isNullOrBlank(serviceName)) {

			this.accept404(session, future, serviceName);

		} else {

			this.accept(serviceName, session, future);
		}
	}

	private void accept(String serviceName, SocketSession session, NamedReadFuture future) throws Exception {

		FutureAcceptorService acceptor = acceptorServiceLoader.getFutureAcceptor(serviceName);

		if (acceptor == null) {

			this.accept404(session, future, serviceName);

		} else {

			future.setIoEventHandle(acceptor);

			acceptor.accept(session, future);
		}
	}

	protected void accept404(SocketSession session, NamedReadFuture future, String serviceName) throws IOException {

		logger.info("service name [{}] not found" , serviceName);

		RESMessage message = new RESMessage(404, "service name not found :" + serviceName);

		flush(session, future, message);
	}

	private void flush(SocketSession session, ReadFuture future, RESMessage message) throws IOException {

		future.setIoEventHandle(this);

		future.write(message.toString());

		session.flush(future);
	}
	
	/**
	 * @param classLoader the classLoader to set
	 */
	protected void setClassLoader(DynamicClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		LifeCycleUtil.stop(acceptorServiceLoader);
	}

	@Override
	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		this.acceptorServiceLoader = new FutureAcceptorServiceLoader(context, classLoader);

		LifeCycleUtil.start(acceptorServiceLoader);
	}

	public FutureAcceptorServiceLoader getFutureAcceptorServiceLoader() {
		return acceptorServiceLoader;
	}

}
