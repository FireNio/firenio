package com.gifisan.nio.component;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.server.InnerResponse;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServletAcceptor;
import com.gifisan.nio.servlet.DeployFilter;
import com.gifisan.nio.servlet.FilterLoader;
import com.gifisan.nio.servlet.NIOFilterWrapper;
import com.gifisan.nio.servlet.NIOFilterWrapperImpl;
import com.gifisan.nio.servlet.NormalFilterLoader;
import com.gifisan.nio.servlet.impl.ErrorServlet;

public final class FilterService extends AbstractLifeCycle implements ServletAcceptor, LifeCycle {

	private static final Logger logger = LoggerFactory.getLogger(FilterService.class);
	private ServerContext		context		= null;
	private NIOFilterWrapper		rootFilter	= null;
	private FilterLoader		filterLoader	= null;
	private DynamicClassLoader	classLoader	= null;

	public FilterService(ServerContext context) {
		this.classLoader = new DynamicClassLoader();
		this.context = context;
	}

	public void accept(Request request, Response response) throws IOException {

		try {
			accept(rootFilter, request, (InnerResponse) response);
		} catch (FlushedException e) {
			logger.error(e.getMessage(),e);
		} catch (NIOException e) {
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			this.acceptException(e, request, response);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			this.acceptException(e, request, response);
		}
	}

	private void acceptException(Exception exception, Request request, Response response) throws IOException {
		ErrorServlet servlet = new ErrorServlet(exception);
		try {
			servlet.accept(request, response);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	private boolean accept(NIOFilterWrapper filter, Request request, InnerResponse response) throws Exception {
		for (; filter != null;) {
			filter.accept(request, response);

			if (response.flushed()) {
				return true;
			}

			filter = filter.nextFilter();
		}
		return true;
	}

	protected void doStart() throws Exception {
		
		this.classLoader.scan(context.getAppLocalAddress());

		this.filterLoader = new NormalFilterLoader(context, classLoader);

		this.filterLoader.start();

		this.rootFilter = filterLoader.getRootFilter();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(filterLoader);
	}

	private void predeploy(DynamicClassLoader classLoader) {
		context.setAttribute("_OLD_ROOT_FILTER", rootFilter);
		NIOFilterWrapper filterWrapper = new NIOFilterWrapperImpl(context, new DeployFilter(),null);
		this.rootFilter = filterWrapper;
	}

	public boolean redeploy() {
		DynamicClassLoader classLoader = new DynamicClassLoader();

		try {
			classLoader.scan(context.getAppLocalAddress());
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			return false;
		}

		if (filterLoader.predeploy(classLoader)) {

			this.predeploy(classLoader);

			this.filterLoader.redeploy(classLoader);

			this.rootFilter = filterLoader.getRootFilter();

			this.subdeploy(classLoader,true);

			return true;
		}

		this.subdeploy(classLoader,false);
		
		return false;

	}

	private void subdeploy(DynamicClassLoader classLoader,boolean success) {
		if (success) {
			context.removeAttribute("_OLD_ROOT_FILTER");
			this.filterLoader.subdeploy(classLoader);
		}else{
			this.rootFilter = (NIOFilterWrapper) context.removeAttribute("_OLD_ROOT_FILTER");
		}
	}

}
