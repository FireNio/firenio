package com.likemessage.server;

import java.sql.SQLException;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.BeanUtil;
import com.generallycloud.nio.common.database.DataBaseContext;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.plugin.jms.MapMessage;
import com.generallycloud.nio.extend.plugin.jms.server.MQContext;
import com.likemessage.bean.T_MESSAGE;

public class MessageServlet extends LMServlet {

	public static final String	SERVICE_NAME		= MessageServlet.class.getSimpleName();

	public static final String	ACTION_ADD_MESSAGE	= "ACTION_ADD_MESSAGE";

	protected AbstractService getAbstractService(DataBaseContext context) throws SQLException {
		return new MessageService(context);
	}

	protected void doAccept(Session session, NIOReadFuture future, AbstractService _service) throws Exception {

//		if (future.hasOutputStream()) {
//
//			OutputStream outputStream = future.getOutputStream();
//
//			if (outputStream == null) {
//				future.setOutputStream(new BufferedOutputStream(future.getStreamLength()), null);
//				return;
//			}
//		}
		
		
		MessageService service = (MessageService) _service;

		Parameters parameters = future.getParameters();

		String action = parameters.getParameter(ACTION);

		if (ACTION_ADD_MESSAGE.equals(action)) {
			addMessage(session, future, parameters, service);
		} else {
			actionNotFound(session, future, _service);
		}

	}

	private void addMessage(Session session, NIOReadFuture future, Parameters parameters, MessageService service)
			throws Exception {

		JSONObject object = parameters.getJSONObject("t_message");

		T_MESSAGE tMessage = (T_MESSAGE) BeanUtil.map2Object(object, T_MESSAGE.class);

		if (service.addMessage(tMessage)) {

		}

		MapMessage _message = new MapMessage("mmm", parameters.getParameter("UUID"));

		_message.put("eventName", "lMessage");
		_message.put("fromUserID", tMessage.getFromUserID());
		_message.put("message", tMessage.getMessage());
		
		MQContext context = MQContext.getInstance();
		
		context.offerMessage(_message);

		RESMessage message = RESMessage.SUCCESS;

		future.write(message.toString());

		session.flush(future);
	}

}
