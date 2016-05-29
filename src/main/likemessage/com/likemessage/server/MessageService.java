package com.likemessage.server;

import java.sql.SQLException;
import java.util.List;

import com.gifisan.database.DataBaseContext;
import com.likemessage.bean.B_Contact;
import com.likemessage.bean.T_MESSAGE;

public class MessageService extends AbstractService {

	protected MessageService(DataBaseContext context) throws SQLException {
		super(context);
	}

	public List<B_Contact> getContactListByUserID(Integer userID) throws SQLException {

		String sql = "select u.userID,u.nickname,u.phoneNo ,u.UUID,c.groupID,cg.groupName,c.backupName from t_contact c left join t_user u on c.friendID = u.userID left join t_contact_group cg on c.groupID = cg.groupID where c.ownerID = ?";

		return query.query(sql, new Object[] { userID }, B_Contact.class);
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

}
