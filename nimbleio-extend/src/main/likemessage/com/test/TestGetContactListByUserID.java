package com.test;

import java.io.IOException;
import java.util.List;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.IOConnectorUtil;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.likemessage.bean.B_Contact;
import com.likemessage.client.LMClient;

public class TestGetContactListByUserID {
	
	
	public static void main(String[] args) throws IOException {

		String username = "wk";
		String password = "wk";

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

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
