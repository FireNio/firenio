package com.generallycloud.nio.extend;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.service.FutureAcceptor;
import com.generallycloud.nio.protocol.ReadFuture;

public class ExtendIOEventHandle extends IoEventHandleAdaptor {

	private ApplicationContext	applicationContext;
	private FutureAcceptor		filterService;
	private Logger				logger	= LoggerFactory.getLogger(ExtendIOEventHandle.class);

	public ExtendIOEventHandle(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void accept(Session session, ReadFuture future) {

		try {

			filterService.accept(session, future);

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			exceptionCaught(session, future, e, IoEventState.HANDLE);
		}
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	protected void doStart() throws Exception {

		LifeCycleUtil.start(applicationContext);

		this.filterService = applicationContext.getFilterService();

		super.doStart();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(applicationContext);

		super.doStop();
	}

}
