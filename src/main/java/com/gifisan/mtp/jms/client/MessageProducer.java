package com.gifisan.mtp.jms.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import test.ClientUtil;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.jms.server.JMSProducerServlet;

public class MessageProducer {

	
	private String serviceName = JMSProducerServlet.SERVICE_NAME;
	
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
	
	public boolean send(String queueName,String content,long timeout) throws IOException{
		
		Map<String, String> param = new HashMap<String, String>();
		param.put("queueName", queueName);
		param.put("content", content);
		String paramString = JSONObject.toJSONString(param);
		
		Response response = client.request(serviceName,paramString , timeout);
		String result = response.getContent();
		return "T".equals(result);
	}
	
	public boolean send(String queueName,String content) throws IOException{
		
		return this.send(queueName,content, 5000);
	}
	
}
