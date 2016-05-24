package com.likemessage.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerContext;

public class UserServlet extends LMServlet {

	public static final String	SERVICE_NAME	= UserServlet.class.getSimpleName();
//	private Logger				logger		= LoggerFactory.getLogger(UserServlet.class);
	private LoginService		loginService	= null;

	protected AbstractService getAbstractService() {
		return loginService;
	}

	protected void doAccept(IOSession session, ServerReadFuture future) throws Exception {

		Parameters parameters = future.getParameters();

		String action = parameters.getParameter("action");

		if ("login".equals(action)) {
			login(session, future, parameters);
		} else if ("logout".equals(action)) {
			logout(session, future, parameters);
		} else if ("reg".equals(action)) {
			reg(session, future, parameters);
		} else {

		}

	}

	private void login(IOSession session, ServerReadFuture future, Parameters parameters) throws Exception {

		boolean login = loginService.login(session, future, parameters);

		future.write(login ? ByteUtil.TRUE : ByteUtil.FALSE);

		session.flush(future);

	}

	private void logout(IOSession session, ServerReadFuture future, Parameters parameters) throws Exception {

		loginService.logout(session);

		future.write(ByteUtil.TRUE);

		session.flush(future);
	}

	private void reg(IOSession session, ServerReadFuture future, Parameters parameters) throws Exception {

		boolean login = loginService.reg(session, future, parameters);

		future.write(login ? ByteUtil.TRUE : ByteUtil.FALSE);

		session.flush(future);
	}

	public void initialize(ServerContext context, Configuration config) throws Exception {
		super.initialize(context, config);

		DataBaseUtil.initializeDataBaseContext();

		loginService = new LoginService(DataBaseUtil.getDataBaseContext());
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {
		super.destroy(context, config);

		DataBaseUtil.destroyDataBaseContext();
	}

}
