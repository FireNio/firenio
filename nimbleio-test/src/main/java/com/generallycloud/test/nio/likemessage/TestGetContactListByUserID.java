package com.generallycloud.test.nio.likemessage;

import java.util.List;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.likemessage.bean.B_Contact;
import com.likemessage.client.LMClient;

public class TestGetContactListByUserID {
	
	
	public static void main(String[] args) throws Exception {

		String username = "wk";
		String password = "wk";

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();
		
		session.login(username, password);
		
		LMClient client = new LMClient();
		
		List<B_Contact> contacts = client.getContactListByUserID(session);
		
		System.out.println(contacts);
		
		ThreadUtil.sleep(500);
		
		CloseUtil.close(connector);
		
	}
}
