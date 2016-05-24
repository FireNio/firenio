package com.likemessage.server;

import java.sql.SQLException;
import java.util.List;

import com.gifisan.database.DataBaseContext;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.likemessage.bean.T_USER;

public class LoginService extends AbstractService{

	private Logger			logger	= LoggerFactory.getLogger(LoginService.class);

	private final String	T_USER	= "T_USER";

	protected LoginService(DataBaseContext context) throws SQLException {
		super(context);
	}

	public boolean existUser(String username) throws SQLException {

		List list = query.query("select count(1) from t_user where username = ?", new Object[] { username });

		return list != null;
	}

	public boolean login(IOSession session, ServerReadFuture future,Parameters parameters) throws SQLException {

		String username = parameters.getParameter("username");

		String password = parameters.getParameter("password");

//		password = MD5Token.getInstance().getLongToken(password, Encoding.DEFAULT);

		return doLogin(session, username, password);
	}
	
	private boolean doLogin(IOSession session,String username,String password) throws SQLException{
		
		List list = query.query("select * from t_user where username = ? and password = ?", new Object[] { username,
				password }, T_USER.class);

		if (list == null) {

			return false;
		}

		session.setAttribute(T_USER, list.get(0));

		return true;
		
	}
	
	public boolean reg(IOSession session, ServerReadFuture future,Parameters parameters) throws SQLException {
		
		String username = parameters.getParameter("username");
		
		String password = parameters.getParameter("password");
		
		if (existUser(username)) {
			return false;
		}
		
		query.executeUpdateSQL("insert into t_user (username,nickname,password)", new Object[] { username,
				username, password });

		return doLogin(session, username, password);
	}
	public boolean logined(IOSession session) {

		return session.getAttribute(T_USER) != null;
	}
	
	public void logout(IOSession session) {
		session.removeAttribute(T_USER);
	}
}
