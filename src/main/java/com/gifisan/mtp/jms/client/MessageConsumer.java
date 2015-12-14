package com.gifisan.mtp.jms.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import test.ClientUtil;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.jms.server.JMSConsumerServlet;
import com.sun.xml.internal.ws.Closeable;

public class MessageConsumer implements Closeable{
	
	
	private String serviceName = JMSConsumerServlet.SERVICE_NAME;
	
	private NIOClient client = ClientUtil.getClient();
	
	public void connect() throws IOException{
		
		client.connect();
	}
	
	
	public void close(){
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String reveice(String queueName,long timeout) throws IOException{
		
		Map<String, String> param = new HashMap<String, String>();
		param.put("queueName", queueName);
		param.put("timeout", String.valueOf(timeout));
		String paramString = JSONObject.toJSONString(param);
		Response response = client.request(serviceName,paramString , timeout);
		return response.getContent();
	}
	
	public String reveice(String queueName) throws IOException{
		
		return this.reveice(queueName,0);
	}
	

}
