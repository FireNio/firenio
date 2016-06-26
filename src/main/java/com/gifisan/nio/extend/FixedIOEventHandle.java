package com.gifisan.nio.extend;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.service.FutureAcceptor;

public class FixedIOEventHandle extends IOEventHandleAdaptor {

	private ApplicationContext	applicationContext;
	private FutureAcceptor		filterService;
	private Logger				logger	= LoggerFactory.getLogger(FixedIOEventHandle.class);

	public FixedIOEventHandle(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void acceptAlong(Session session, ReadFuture future) {

		try {

			filterService.accept(session, future);

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			exceptionCaughtOnWrite(session, future, null, e);
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
