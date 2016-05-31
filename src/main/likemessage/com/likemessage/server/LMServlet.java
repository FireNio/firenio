package com.likemessage.server;

import java.sql.SQLException;

import com.gifisan.database.DataBaseContext;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.service.NIOServlet;

public abstract class LMServlet extends NIOServlet {
	
	public static final String	ACTION		= "ACTION";

	protected abstract AbstractService getAbstractService(DataBaseContext context) throws SQLException;

	public void accept(IOSession session, ServerReadFuture future) throws Exception {

		AbstractService service = getAbstractService(DataBaseUtil.getDataBaseContext());

		try {

			service.open();

			doAccept(session, future, service);

		} finally {
			service.close();
		}
	}

	protected abstract void doAccept(IOSession session, ServerReadFuture future, AbstractService _service)
			throws Exception;
	
	
	protected void actionNotFound(IOSession session, ServerReadFuture future, AbstractService _service){
		
		RESMessage message = RESMessage.EMPTY_404;
		
		future.write(message.toString());
		
		session.flush(future);
	}

}
