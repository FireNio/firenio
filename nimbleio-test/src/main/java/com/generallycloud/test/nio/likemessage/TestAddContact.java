package com.generallycloud.test.nio.likemessage;

import java.util.Map;

import com.generallycloud.nio.common.BeanUtil;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.likemessage.bean.T_CONTACT;
import com.likemessage.bean.T_USER;
import com.likemessage.client.LMClient;

public class TestAddContact {

	public static void main(String[] args) throws Exception {

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("wk", "wk");
		
		LMClient client = new LMClient();
		
		T_CONTACT contact = new T_CONTACT();
		
		contact.setBackupName("张飞");
		contact.setGroupID(0);
		contact.setOwnerID(1);
		contact.setPinyin("zhangfei");
		
		String friendName = "zhangfei";

		RESMessage message = client.addContact(session, contact, friendName);

		if (message.getCode() == 0) {
			
			T_USER user = (T_USER) BeanUtil.map2Object((Map)message.getData(), T_USER.class);
			
			System.out.println(user);
		}
		
		System.out.println(message);

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
