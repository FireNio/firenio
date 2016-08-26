package com.gifisan.nio.extend.service;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.HotDeploy;
import com.gifisan.nio.extend.Initializeable;
import com.gifisan.nio.extend.InitializeableImpl;
import com.gifisan.nio.extend.configuration.Configuration;

public abstract class FutureAcceptorService extends InitializeableImpl implements Initializeable, HotDeploy,
		IOEventHandle {

	private Logger	logger	= LoggerFactory.getLogger(FutureAcceptorService.class);

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {

	}

	public void prepare(ApplicationContext context, Configuration config) throws Exception {
		this.initialize(context, config);
	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {
		this.destroy(context, config);
	}

	public void futureSent(Session session, ReadFuture future) {

	}
	
	public void exceptionCaught(Session session, ReadFuture future, Exception cause, IOEventState state) {
		logger.error(cause.getMessage(), cause);
	}

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
