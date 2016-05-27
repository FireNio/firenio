package com.likemessage.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.RESMessageDecoder;
import com.likemessage.server.ContactServlet;
import com.likemessage.server.LMServlet;
import com.likemessage.server.UserServlet;

public class LMClient {

	public boolean regist(ClientSession session, String username, String password) throws IOException {
		
		String serviceKey = UserServlet.SERVICE_NAME;
		
		JSONObject o = new JSONObject();
		
		o.put(LMServlet.ACTION, UserServlet.ACTION_REGIST);
		o.put("username", username);
		o.put("password", MD5Token.getInstance().getLongToken(password, Encoding.DEFAULT));

		ReadFuture future = session.request(serviceKey, o.toJSONString());

		return ByteUtil.isTrue(future.getText());

	}
	
	public boolean login(ClientTCPConnector connector, String username, String password) throws IOException {
		
		return connector.login(username, password);
	}
	
	public List<Map> getContactListByUserID(ClientSession session) throws IOException{
		
		String serviceKey = ContactServlet.SERVICE_NAME;
		
		JSONObject o = new JSONObject();
		
		o.put(LMServlet.ACTION, ContactServlet.ACTION_GETCONTACTLISTBYUSERID);

		ReadFuture future = session.request(serviceKey, o.toJSONString());

		RESMessage message = RESMessageDecoder.decode(future.getText());
		
		if (message.getCode() == 0) {
			return (List<Map>) message.getData();
		}
		
		throw new IOException(message.getDescription());
	}
	
	

}
