package com.gifisan.nio.extend.service;

import java.io.IOException;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.DynamicClassLoader;
import com.gifisan.nio.extend.RESMessage;
import com.gifisan.nio.extend.configuration.Configuration;

public final class FutureAcceptorServiceFilter extends FutureAcceptorFilter {

	private DynamicClassLoader			classLoader			;
	private Logger						logger				= LoggerFactory.getLogger(FutureAcceptorServiceFilter.class);
	private FutureAcceptorServiceLoader	acceptorServiceLoader	;

	public FutureAcceptorServiceFilter(DynamicClassLoader classLoader) {
		this.classLoader = classLoader;
		this.setSortIndex(Integer.MAX_VALUE);
	}

	public void accept(Session session, ReadFuture future) throws Exception {

		String serviceName = future.getServiceName();

		if (StringUtil.isNullOrBlank(serviceName)) {

			this.accept404(session, future);

		} else {

			this.accept(serviceName, session, future);
		}
	}

	private void accept(String serviceName, Session session, ReadFuture future) throws Exception {

		FutureAcceptorService acceptor = acceptorServiceLoader.getFutureAcceptor(serviceName);

		if (acceptor == null) {

			this.accept404(session, future, serviceName);

		} else {
			
			future.setIOEventHandle(acceptor);

			acceptor.accept(session, future);
		}
	}

	private void accept404(Session session, ReadFuture future) throws IOException {

		logger.info("[NIOServer] empty service name");

		flush(session, future, RESMessage.EMPTY_404);
	}

	private void accept404(Session session, ReadFuture future, String serviceName) throws IOException {

		logger.info("[NIOServer] 未发现命令：" + serviceName);
		
		RESMessage message = new RESMessage(404, "service name not found :" + serviceName);

		flush(session, future, message);
	}
	
	private void flush(Session session, ReadFuture future, RESMessage message){
		
		future.setIOEventHandle(this);
		
		future.write(message.toString());

		session.flush(future);
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		LifeCycleUtil.stop(acceptorServiceLoader);

	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		this.acceptorServiceLoader = new FutureAcceptorServiceLoader(context, classLoader);

		LifeCycleUtil.start(acceptorServiceLoader);
	}

	public void prepare(ApplicationContext context, Configuration config) throws Exception {

		this.acceptorServiceLoader = new FutureAcceptorServiceLoader(context, classLoader);

		this.acceptorServiceLoader.prepare(context, config);

	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {
		this.acceptorServiceLoader.unload(context, config);
	}

	public FutureAcceptorServiceLoader getFutureAcceptorServiceLoader() {
		return acceptorServiceLoader;
	}

}
