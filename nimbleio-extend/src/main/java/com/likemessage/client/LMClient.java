package com.likemessage.client;

import java.io.IOException;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.MD5Token;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.RESMessageDecoder;
import com.likemessage.bean.B_Contact;
import com.likemessage.bean.T_CONTACT;
import com.likemessage.bean.T_MESSAGE;
import com.likemessage.server.ContactServlet;
import com.likemessage.server.LMServlet;
import com.likemessage.server.MessageServlet;
import com.likemessage.server.UserServlet;

public class LMClient {

	public RESMessage regist(FixedSession session, String username, String password,String nickname) throws IOException {
		
		String serviceKey = UserServlet.SERVICE_NAME;
		
		JSONObject o = new JSONObject();
		
		o.put(LMServlet.ACTION, UserServlet.ACTION_REGIST);
		o.put("username", username);
		o.put("nickname", nickname);
		o.put("password", MD5Token.getInstance().getLongToken(password, session.getContext().getEncoding()));

		NIOReadFuture future = session.request(serviceKey, o.toJSONString());

		return RESMessageDecoder.decode(future.getText());

	}
	
	public List<B_Contact> getContactListByUserID(FixedSession session) throws IOException{
		
		String serviceKey = ContactServlet.SERVICE_NAME;
		
		JSONObject o = new JSONObject();
		
		o.put(LMServlet.ACTION, ContactServlet.ACTION_GETCONTACTLISTBYUSERID);

		NIOReadFuture future = session.request(serviceKey, o.toJSONString());

		RESMessage message = RESMessageDecoder.decode(future.getText());
		
		if (message.getCode() == 0) {
			JSONArray array = (JSONArray) message.getData();
			
			if (array == null) {
				return null;
			}
			
			return JSONArray.parseArray(array.toJSONString(), B_Contact.class);
		}
		
		throw new IOException(message.getDescription());
	}
	
	public RESMessage addMessage(FixedSession session,T_MESSAGE message,String UUID) throws IOException{
		
		String serviceKey = MessageServlet.SERVICE_NAME;
		
		JSONObject o = new JSONObject();
		
		o.put(LMServlet.ACTION, MessageServlet.ACTION_ADD_MESSAGE);
		o.put("UUID", UUID);
		o.put("t_message", message);

		NIOReadFuture future = session.request(serviceKey, o.toJSONString());

		return RESMessageDecoder.decode(future.getText());
	}
	
	public RESMessage addContact(FixedSession session,T_CONTACT contact,String friendName) throws IOException{
		
		String serviceKey = ContactServlet.SERVICE_NAME;
		
		JSONObject o = new JSONObject();
		
		o.put(LMServlet.ACTION, ContactServlet.ACTION_ADD_CONTACT);
		o.put("t_contact", contact);
		o.put("friendName", friendName);

		NIOReadFuture future = session.request(serviceKey, o.toJSONString());

		return RESMessageDecoder.decode(future.getText());
	}
	

}
