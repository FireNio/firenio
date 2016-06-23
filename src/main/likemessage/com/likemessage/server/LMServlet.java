package com.likemessage.server;

import java.sql.SQLException;

import com.gifisan.database.DataBaseContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.service.NIOFutureAcceptor;

public abstract class LMServlet extends NIOFutureAcceptor {
	
	public static final String	ACTION		= "ACTION";

	protected abstract AbstractService getAbstractService(DataBaseContext context) throws SQLException;

	public void accept(Session session, ReadFuture future) throws Exception {

		AbstractService service = getAbstractService(DataBaseUtil.getDataBaseContext());

		try {

			service.open();

			doAccept(session, future, service);

		} finally {
			service.close();
		}
	}

	protected abstract void doAccept(Session session, ReadFuture future, AbstractService _service)
			throws Exception;
	
	
	protected void actionNotFound(Session session, ReadFuture future, AbstractService _service){
		
		RESMessage message = RESMessage.EMPTY_404;
		
		future.write(message.toString());
		
		session.flush(future);
	}

}
