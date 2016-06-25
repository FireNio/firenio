package com.gifisan.nio.component;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.server.service.FutureAcceptor;

public class FixedIOEventHandle extends IOEventHandleAdaptor {

	private ApplicationContext	applicationContext	= null;
	private FutureAcceptor		filterService		= null;
	private Logger				logger			= LoggerFactory.getLogger(FixedIOEventHandle.class);

	public FixedIOEventHandle(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void accept(Session session, ReadFuture future) throws Exception {

		try {
			filterService.accept(session, future);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			exceptionCaughtOnWrite(session, future, null, e);
		}
	}

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) {
		DatagramPacketAcceptor acceptor = applicationContext.getDatagramPacketAcceptor();

		if (acceptor == null) {
			logger.debug("___________________null acceptor,packet:{}", packet);
			return;
		}

		// logger.debug("___________________client receive,packet:{}",packet);

		try {
			acceptor.accept(endPoint, packet);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	protected void doStart() throws Exception {
		
		this.applicationContext.start();

		this.filterService = applicationContext.getFilterService();
		
		super.doStart();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(applicationContext);
		
		super.doStop();
	}
	
	

}
