package com.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.likemessage.client.LMClient;

public class TestGetContactListByUserID {
	
	
	public static void main(String[] args) throws IOException {

		String username = "wk";
		String password = "wk";

		final ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		connector.login(username, password);
		
		ClientSession session = connector.getClientSession();
		
		LMClient client = new LMClient();
		
		List<Map> contacts = client.getContactListByUserID(session);
		
		System.out.println(contacts);
		
		ThreadUtil.sleep(500);
		
		CloseUtil.close(connector);
		
	}
}
