package com.likemessage.server;

import java.sql.SQLException;
import java.util.List;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.BeanUtil;
import com.generallycloud.nio.common.database.DataBaseContext;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.ApplicationContextUtil;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.security.Authority;
import com.generallycloud.nio.protocol.ReadFuture;
import com.likemessage.bean.B_Contact;
import com.likemessage.bean.T_CONTACT;

public class ContactServlet extends LMServlet {

	public static final String	SERVICE_NAME				= ContactServlet.class.getSimpleName();

	public static final String	ACTION_GETCONTACTLISTBYUSERID	= "ACTION_GETCONTACTLISTBYUSERID";
	
	public static final String ACTION_ADD_CONTACT = "ACTION_ADD_CONTACT";

	protected AbstractService getAbstractService(DataBaseContext context) throws SQLException {
		return new ContactService(context);
	}

	protected void doAccept(Session session, NIOReadFuture future, AbstractService _service) throws Exception {

		ContactService service = (ContactService) _service;

		Parameters parameters = future.getParameters();

		String action = parameters.getParameter(ACTION);

		if (ACTION_GETCONTACTLISTBYUSERID.equals(action)) {
			getContactListByUserID(session, future, parameters, service);
		} else if(ACTION_ADD_CONTACT.equals(action)){
			addContact(session, future, parameters, service);
		} else {
			actionNotFound(session, future, _service);
		}
	}

	private void getContactListByUserID(Session session, ReadFuture future, Parameters parameters,
			ContactService service) throws Exception {

		Authority authority = ApplicationContextUtil.getAuthority(session);
		
		Integer userID = authority.getUserID();

		List<B_Contact> contactList = service.getContactListByUserID(userID);

		RESMessage message = new RESMessage(0, contactList, null);

		future.write(message.toString());

		session.flush(future);
	}
	
	private void addContact(Session session, ReadFuture future, Parameters parameters,
			ContactService service) throws Exception {

		T_CONTACT contact = (T_CONTACT) BeanUtil.map2Object(parameters.getJSONObject("t_contact"), T_CONTACT.class);

		Authority authority = ApplicationContextUtil.getAuthority(session);
		
		contact.setOwnerID(authority.getUserID());
		
		String friendName = parameters.getParameter("friendName");
		
		RESMessage message = service.addContact(contact,friendName);
		
		future.write(message.toString());

		session.flush(future);
	}

}
