package com.likemessage.server;

import java.sql.SQLException;
import java.util.List;

import com.generallycloud.nio.common.database.DataBaseContext;
import com.likemessage.bean.T_MESSAGE;

public class MessageService extends AbstractService {

	protected MessageService(DataBaseContext context) throws SQLException {
		super(context);
	}

	public boolean addMessage(T_MESSAGE message) throws SQLException {

		String sql = "insert into t_message (toUserID,fromUserID,msgDate,msgType,message,isSend,deleted) values(?,?,?,?,?,?,?)";

		return query.executeUpdateSQL(sql, new Object[]{
				message.getToUserID(),
				message.getFromUserID(),
				message.getMsgDate(),
				message.getMsgType(),
				message.getMessage(),
				message.isSend(),
				message.isDeleted()
		}) == 1;
		
	}
	
	public List<T_MESSAGE> getMessageListByUserID(Integer userID) throws SQLException{
		
		String sql = "";
		
		return query.executeQueryCall(sql, new Object[]{userID}, T_MESSAGE.class);
		
	}

}
