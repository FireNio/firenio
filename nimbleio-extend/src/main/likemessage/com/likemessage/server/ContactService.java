package com.likemessage.server;

import java.sql.SQLException;
import java.util.List;

import com.generallycloud.nio.common.database.DataBaseContext;
import com.generallycloud.nio.extend.RESMessage;
import com.likemessage.bean.B_Contact;
import com.likemessage.bean.T_CONTACT;
import com.likemessage.bean.T_USER;

public class ContactService extends AbstractService {

	protected ContactService(DataBaseContext context) throws SQLException {
		super(context);
	}


	public List<B_Contact> getContactListByUserID(Integer userID) throws SQLException {

		List<B_Contact> list = (List<B_Contact>) query
				.query("select u.userID,u.nickname,u.phoneNo ,u.UUID,c.groupID,cg.groupName,c.backupName,c.pinyin from t_contact c left join t_user u on c.friendID = u.userID left join t_contact_group cg on c.groupID = cg.groupID where c.ownerID = ?",
						new Object[] { userID }, B_Contact.class);
		return list;
	}
	
	public RESMessage addContact(T_CONTACT contact,String friendName) throws SQLException{
		
		Integer ownerID = contact.getOwnerID();
		
		T_USER friend = getUser(friendName);
		
		if (friend == null) {
			return RESMessage.USER_NOT_EXIST;
		}
		
		if (existContact(ownerID, friend.getUserID())) {
			return RESMessage.CONTACT_EXIST;
		}
		
		String sql = "INSERT INTO T_CONTACT (OWNERID,FRIENDID,BACKUPNAME,PINYIN,GROUPID) VALUES(?,?,?,?,0)";
		
		if(query.executeUpdateSQL(sql, new Object[]{ownerID,friend.getUserID(),contact.getBackupName(),contact.getPinyin()}) == 1){
			
			sql = "select * from t_user where username";

			friend.setPassword(null);
			
			friend.setUsername(null);
			
			return new RESMessage(0, friend, null);
		}

		return RESMessage.SYSTEM_ERROR;
	}
	
	private boolean existContact(Integer ownerID,Integer friendID) throws SQLException{
		
		String sql = "select count(1) count from T_CONTACT where ownerID = ? and friendID = ?";
		
		return query.queryCount(sql, new Object[]{ownerID,friendID}) > 0;
	}
	
	private T_USER getUser(String friendName) throws SQLException{
		
		String sql = "select * from t_user where username = ?";
		
		List<T_USER> list = query.query(sql, new Object[]{friendName},T_USER.class);
		
		if (list == null) {
			return null;
		}
		
		return list.get(0);
	}
}
