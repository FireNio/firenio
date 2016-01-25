package com.gifisan.mtp.component;

import java.io.IOException;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.server.InnerResponse;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerContext;
import com.gifisan.mtp.server.ServletAcceptor;
import com.gifisan.mtp.servlet.DebugFilterLoader;
import com.gifisan.mtp.servlet.FilterLoader;
import com.gifisan.mtp.servlet.MTPFilterWrapper;
import com.gifisan.mtp.servlet.NormalFilterLoader;
import com.gifisan.mtp.servlet.impl.ErrorServlet;

public final class FilterService extends AbstractLifeCycle implements ServletAcceptor, LifeCycle {

	private ServerContext		context		= null;
	private MTPFilterWrapper		rootFilter	= null;
	private FilterLoader		filterLoader	= null;
	private DynamicClassLoader	classLoader	= null;

	public FilterService(ServerContext context) {
		this.classLoader = new DynamicClassLoader();
		this.context = context;
	}

	public void accept(Request request, Response response) throws IOException {
		
		try {
			accept(rootFilter,request, response);
		} catch (FlushedException e) {
			e.printStackTrace();
		} catch (MTPChannelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			this.acceptException(e, request, response);
		} catch (Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}
	
	private boolean accept(MTPFilterWrapper filter,Request request, Response response) throws Exception{
		InnerResponse _response = (InnerResponse) response;
		for (; filter != null ;) {
			filter.accept(request, _response);
			
			if (_response.flushed()) {
				return true;
			}
			
			filter = filter.nextFilter();
		}
		return true;
	}

	protected void doStart() throws Exception {
		boolean debug = SharedBundle.instance().getBooleanProperty("SERVER.DEBUG");

		this.filterLoader = debug ? new DebugFilterLoader(context,classLoader) : new NormalFilterLoader(context, classLoader);

		this.filterLoader.start();

		this.rootFilter = filterLoader.getRootFilter();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(filterLoader);
	}

	public boolean redeploy() {
		if (filterLoader.redeploy(classLoader)) {
			MTPFilterWrapper _rootFilter = rootFilter;
			this.rootFilter = filterLoader.getRootFilter();
			LifeCycleUtil.stop(_rootFilter);
			return true;
		}
		return false;
	}

}
