package com.likemessage.server;

import java.sql.SQLException;
import java.util.List;

import com.gifisan.database.DataBaseContext;
import com.likemessage.bean.B_Contact;

public class ContactService extends AbstractService {

	protected ContactService(DataBaseContext context) throws SQLException {
		super(context);
	}


	public List<B_Contact> getContactListByUserID(Integer userID) throws SQLException {

		List<B_Contact> list = (List<B_Contact>) query
				.query("select u.userID,u.nickname,u.phoneNo ,u.UUID,c.groupID,cg.groupName,c.backupName from t_contact c left join t_user u on c.friendID = u.userID left join t_contact_group cg on c.groupID = cg.groupID where c.ownerID = ?",
						new Object[] { userID }, B_Contact.class);
		return list;
	}

	
	
	

}
