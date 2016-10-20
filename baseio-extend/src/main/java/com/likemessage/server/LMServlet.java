package com.likemessage.server;

import java.io.IOException;
import java.sql.SQLException;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.database.DataBaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.service.BaseFutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class LMServlet extends BaseFutureAcceptorService {

	public static final String	ACTION	= "ACTION";

	protected abstract AbstractService getAbstractService(DataBaseContext context) throws SQLException;

	public void doAccept(Session session, BaseReadFuture future) throws Exception {
		AbstractService service = getAbstractService(DataBaseUtil.getDataBaseContext());

		try {

			service.open();

			doAccept(session, future, service);

		} finally {
			service.close();
		}

	}

	protected abstract void doAccept(Session session, BaseReadFuture future, AbstractService _service) throws Exception;

	protected void actionNotFound(Session session, ReadFuture future, AbstractService _service) throws IOException {

		RESMessage message = RESMessage.EMPTY_404;

		future.write(message.toString());

		session.flush(future);
	}

}
