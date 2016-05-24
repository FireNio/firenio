package com.likemessage.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.NIOServlet;

public abstract class LMServlet extends NIOServlet {

	protected abstract AbstractService getAbstractService();

	public void accept(IOSession session, ServerReadFuture future) throws Exception {

		try {

			getAbstractService().open();

			doAccept(session, future);

		} finally {
			getAbstractService().close();
		}
	}

	protected abstract void doAccept(IOSession session, ServerReadFuture future) throws Exception;

}
