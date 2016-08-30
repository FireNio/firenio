package com.likemessage.server;

import java.io.IOException;
import java.sql.SQLException;

import com.gifisan.nio.common.database.DataBaseContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.RESMessage;
import com.gifisan.nio.extend.service.NIOFutureAcceptorService;

public abstract class LMServlet extends NIOFutureAcceptorService {

	public static final String	ACTION	= "ACTION";

	protected abstract AbstractService getAbstractService(DataBaseContext context) throws SQLException;

	public void doAccept(Session session, NIOReadFuture future) throws Exception {
		AbstractService service = getAbstractService(DataBaseUtil.getDataBaseContext());

		try {

			service.open();

			doAccept(session, future, service);

		} finally {
			service.close();
		}

	}

	protected abstract void doAccept(Session session, NIOReadFuture future, AbstractService _service) throws Exception;

	protected void actionNotFound(Session session, ReadFuture future, AbstractService _service) throws IOException {

		RESMessage message = RESMessage.EMPTY_404;

		future.write(message.toString());

		session.flush(future);
	}

}
