package com.test;

import java.io.IOException;
import java.util.List;

import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.likemessage.bean.B_Contact;
import com.likemessage.client.LMClient;

public class TestGetContactListByUserID {
	
	
	public static void main(String[] args) throws IOException {

		String username = "wk";
		String password = "wk";

		final TCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		connector.login(username, password);
		
		FixedSession session = connector.getClientSession();
		
		LMClient client = new LMClient();
		
		List<B_Contact> contacts = client.getContactListByUserID(session);
		
		System.out.println(contacts);
		
		ThreadUtil.sleep(500);
		
		CloseUtil.close(connector);
		
	}
}
