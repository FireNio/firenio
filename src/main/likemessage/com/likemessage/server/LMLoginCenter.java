package com.likemessage.server;

import java.sql.SQLException;
import java.util.List;

import com.gifisan.database.DataBaseContext;
import com.gifisan.database.SessionQuery;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.security.Authority;
import com.gifisan.security.AuthorityLoginCenter;
import com.likemessage.bean.T_USER;

public class LMLoginCenter extends AuthorityLoginCenter {

	private SessionQuery	query	= null;
	private Logger			logger	= LoggerFactory.getLogger(LMLoginCenter.class);

	protected void initialize(DataBaseContext context) {
		this.query = new SessionQuery(context);
	}

	protected Authority getAuthority(Session session, ReadFuture future) {
		try {
			query.open();

			Parameters parameters = future.getParameters();

			String username = parameters.getParameter("username");

			String password = parameters.getParameter("password");

			List<T_USER> list = query.query("select * from t_user where username = ? and password = ?", new Object[] {
					username, password }, T_USER.class);

			if (list != null) {

				return list.get(0);
			}

		} catch (SQLException e) {

			logger.error(e.getMessage(), e);
		} finally {

			query.close();
		}

		return super.getAuthority(session, future);
	}
	

}
