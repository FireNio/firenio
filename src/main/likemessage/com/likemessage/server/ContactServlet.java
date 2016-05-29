package com.likemessage.server;

import java.sql.SQLException;
import java.util.List;

import com.gifisan.database.DataBaseContext;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.RESMessage;
import com.likemessage.bean.B_Contact;

public class ContactServlet extends LMServlet {

	public static final String	SERVICE_NAME				= ContactServlet.class.getSimpleName();

	public static final String	ACTION_GETCONTACTLISTBYUSERID	= "ACTION_GETCONTACTLISTBYUSERID";

	protected AbstractService getAbstractService(DataBaseContext context) throws SQLException {
		return new ContactService(context);
	}

	protected void doAccept(IOSession session, ServerReadFuture future, AbstractService _service) throws Exception {

		ContactService service = (ContactService) _service;

		Parameters parameters = future.getParameters();

		String action = parameters.getParameter(ACTION);

		if (ACTION_GETCONTACTLISTBYUSERID.equals(action)) {
			getContactListByUserID(session, future, parameters, service);
		} else {

		}

	}

	private void getContactListByUserID(IOSession session, ServerReadFuture future, Parameters parameters,
			ContactService service) throws Exception {

		Integer userID = session.getAuthority().getUserID();

		List<B_Contact> contactList = service.getContactListByUserID(userID);

		RESMessage message = new RESMessage(0, contactList, null);

		future.write(message.toString());

		session.flush(future);
	}

}
