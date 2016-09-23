package com.likemessage.server;

import java.sql.SQLException;
import java.util.Properties;

import com.alibaba.fastjson.JSONArray;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.database.DataBaseContext;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.configuration.Configuration;

public class UserServlet extends LMServlet {

	public static final String	SERVICE_NAME	= UserServlet.class.getSimpleName();

	public static final String	ACTION_REGIST	= "ACTION_REGIST";

	protected AbstractService getAbstractService(DataBaseContext context) throws SQLException {
		return new UserService(context);
	}

	protected void doAccept(Session session, NIOReadFuture future, AbstractService _service) throws Exception {

		UserService service = (UserService) _service;

		Parameters parameters = future.getParameters();

		String action = parameters.getParameter(ACTION);

		if (ACTION_REGIST.equals(action)) {
			regist(session, future, parameters, service);
		} else {
			actionNotFound(session, future, _service);
		}
	}

	private void regist(Session session, NIOReadFuture future, Parameters parameters, UserService service)
			throws Exception {

		RESMessage message = service.regist(session, future, parameters);
		
		future.write(message.toString());

		session.flush(future);
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		super.initialize(context, config);

		Properties p = SharedBundle.instance().loadProperties(config.getParameter("data-source"));
		
		DataBaseUtil.initializeDataBaseContext(p);
		
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
