package com.likemessage.server;

import java.sql.SQLException;

import com.alibaba.fastjson.JSONArray;
import com.gifisan.database.DataBaseContext;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.RESMessage;
import com.gifisan.nio.extend.configuration.Configuration;

public class UserServlet extends LMServlet {

	public static final String	SERVICE_NAME	= UserServlet.class.getSimpleName();

	public static final String	ACTION_REGIST	= "ACTION_REGIST";

	protected AbstractService getAbstractService(DataBaseContext context) throws SQLException {
		return new UserService(context);
	}

	protected void doAccept(Session session, ReadFuture future, AbstractService _service) throws Exception {

		UserService service = (UserService) _service;

		Parameters parameters = future.getParameters();

		String action = parameters.getParameter(ACTION);

		if (ACTION_REGIST.equals(action)) {
			regist(session, future, parameters, service);
		} else {
			actionNotFound(session, future, _service);
		}
	}

	private void regist(Session session, ReadFuture future, Parameters parameters, UserService service)
			throws Exception {

		RESMessage message = service.regist(session, future, parameters);
		
		future.write(message.toString());

		session.flush(future);
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		super.initialize(context, config);

		DataBaseUtil.initializeDataBaseContext();
		
		LMLoginCenter loginCenter = (LMLoginCenter) context.getLoginCenter();

		DataBaseContext dataBaseContext = DataBaseUtil.getDataBaseContext();
		
		JSONArray beans = config.getJSONArray("beans");
		
		for (int i = 0; i < beans.size(); i++) {

			String className = beans.getString(i);
			
//			Class clazz = ClassUtil.forName(className);
				
			Class clazz = context.getClassLoader().loadClass(className);
			
			dataBaseContext.registBean(clazz);
		}

		loginCenter.initialize(dataBaseContext);
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {

		super.destroy(context, config);

		DataBaseUtil.destroyDataBaseContext();
	}

}
