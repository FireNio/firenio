package com.likemessage.server;

import java.sql.SQLException;
import java.util.List;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.InitializeableImpl;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerContext;
import com.likemessage.bean.T_USER;

public class LMLoginCenter extends InitializeableImpl implements LoginCenter{
	
	

	public void initialize(ServerContext context, Configuration config) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public boolean isLogined(IOSession session) {
		// TODO Auto-generated method stub
		return false;
	}

	public void logout(IOSession session) {
		// TODO Auto-generated method stub
		
	}

	public boolean isValidate(IOSession session, ServerReadFuture future) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean login(IOSession session, ServerReadFuture future) {
		Parameters parameters = future.getParameters();
		
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
	
}
