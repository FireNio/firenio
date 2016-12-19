package com.generallycloud.nio.container;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptor;
import com.generallycloud.nio.protocol.ReadFuture;

public class ExtendIOEventHandle extends IoEventHandleAdaptor {

	private ApplicationContext	applicationContext;
	private FutureAcceptor		filterService;
	private Logger				logger	= LoggerFactory.getLogger(ExtendIOEventHandle.class);

	public ExtendIOEventHandle(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void accept(SocketSession session, ReadFuture future) {

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

	@Override
	protected void doStart() throws Exception {

		LifeCycleUtil.start(applicationContext);

		this.filterService = applicationContext.getFilterService();

		super.doStart();
	}

	@Override
	protected void doStop() throws Exception {
		LifeCycleUtil.stop(applicationContext);

		super.doStop();
	}

}
