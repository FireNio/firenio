package com.gifisan.nio.extend.service;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.WriteFuture;
import com.gifisan.nio.component.future.nio.NIOReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.HotDeploy;
import com.gifisan.nio.extend.Initializeable;
import com.gifisan.nio.extend.InitializeableImpl;
import com.gifisan.nio.extend.configuration.Configuration;
import com.gifisan.nio.extend.implementation.ErrorServlet;

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

	public void exceptionCaughtOnRead(Session session, ReadFuture future, Exception cause) {
		this.acceptException(session, future, cause);
	}

	private void acceptException(Session session, ReadFuture future, Throwable exception) {

		ErrorServlet servlet = new ErrorServlet(exception);

		try {

			servlet.accept(session, future);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);
		}
	}

	public void exceptionCaughtOnWrite(Session session, ReadFuture readFuture, WriteFuture writeFuture, Exception cause) {
		logger.error(cause.getMessage(), cause);
	}

	public void futureSent(Session session, WriteFuture future) {

	}
	
	public void accept(Session session, ReadFuture future) throws Exception {
		this.doAccept(session, (NIOReadFuture) future);
	}
	
	protected abstract void doAccept(Session session,NIOReadFuture future) throws Exception;

	public String toString() {

		Configuration configuration = this.getConfig();

		String serviceName = null;

		if (configuration == null) {

			serviceName = this.getClass().getSimpleName();
		} else {

			serviceName = configuration.getParameter("serviceName");

			if (StringUtil.isNullOrBlank(serviceName)) {
				serviceName = this.getClass().getSimpleName();
			}
		}

		return "(service-name:" + serviceName + "@class:" + this.getClass().getName() + ")";
	}

}
