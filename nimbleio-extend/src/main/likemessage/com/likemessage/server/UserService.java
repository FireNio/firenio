package com.likemessage.server;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.generallycloud.nio.common.UUIDGenerator;
import com.generallycloud.nio.common.database.DataBaseContext;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.extend.ApplicationContextUtil;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.security.Authority;

public class UserService extends AbstractService{

	protected UserService(DataBaseContext context) throws SQLException {
		super(context);
	}

	public boolean existUser(String username) throws SQLException {

		List<Map<String,Object>> list = query.query("select count(1) count from t_user where username = ?", new Object[] { username });

		return ((Long)list.get(0).get("count")) == 1;
	}

	public RESMessage regist(Session session, ReadFuture future,Parameters parameters) throws SQLException {
		
		String username = parameters.getParameter("username");
		
		String password = parameters.getParameter("password");
		
		String nickname = parameters.getParameter("nickname");
		
		if (existUser(username)) {
			return RESMessage.USER_EXIST;
		}
		
		int result = query.executeUpdateSQL("insert into t_user (username,nickname,password,roleID,UUID) values (?,?,?,?,?)", 
				new Object[] { username,	nickname, password,4,UUIDGenerator.random() });
		
		if (result == 1) {
			
			return RESMessage.SUCCESS;
		}

		return RESMessage.SYSTEM_ERROR;
	}
	
	public boolean logined(Session session) {
		
		Authority authority = ApplicationContextUtil.getAuthority(session);

		return authority.getRoleID() != Authority.GUEST.getRoleID();
	}
	
}
