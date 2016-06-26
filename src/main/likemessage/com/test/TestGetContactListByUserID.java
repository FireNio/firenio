package com.test;

import java.io.IOException;
import java.util.List;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.likemessage.bean.B_Contact;
import com.likemessage.client.LMClient;

public class TestGetContactListByUserID {
	
	
	public static void main(String[] args) throws IOException {

		String username = "wk";
		String password = "wk";

		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();
		
		session.login(username, password);
		
		LMClient client = new LMClient();
		
		List<B_Contact> contacts = client.getContactListByUserID(session);
		
		System.out.println(contacts);
		
		ThreadUtil.sleep(500);
		
		CloseUtil.close(connector);
		
	}
}
