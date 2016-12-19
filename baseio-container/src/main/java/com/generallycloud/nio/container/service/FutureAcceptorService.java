package com.generallycloud.nio.container.service;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.IoEventHandle;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.HotDeploy;
import com.generallycloud.nio.container.Initializeable;
import com.generallycloud.nio.container.InitializeableImpl;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class FutureAcceptorService extends InitializeableImpl implements Initializeable, HotDeploy,
		IoEventHandle {

	private Logger	logger	= LoggerFactory.getLogger(FutureAcceptorService.class);

	@Override
	public void initialize(ApplicationContext context, Configuration config) throws Exception {

	}

	@Override
	public void prepare(ApplicationContext context, Configuration config) throws Exception {
		this.initialize(context, config);
	}

	@Override
	public void unload(ApplicationContext context, Configuration config) throws Exception {
		this.destroy(context, config);
	}

	@Override
	public void futureSent(SocketSession session, ReadFuture future) {

	}
	
	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		logger.error(cause.getMessage(), cause);
	}

	@Override
	public String toString() {

		Configuration configuration = this.getConfig();

		String serviceName = null;

		if (configuration == null) {

			serviceName = this.getClass().getSimpleName();
		} else {

			serviceName = configuration.getParameter("service-name");

			if (StringUtil.isNullOrBlank(serviceName)) {
				serviceName = this.getClass().getSimpleName();
			}
		}

		return "(service-name:" + serviceName + "@class:" + this.getClass().getName() + ")";
	}

}
