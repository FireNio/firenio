package com.gifisan.nio.extend.plugin.jms.decode;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.plugin.jms.MapByteMessage;
import com.gifisan.nio.extend.plugin.jms.Message;

public class MapByteMessageDecoder implements MessageDecoder{

	public Message decode(ReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		JSONObject map = param.getJSONObject("map");
		
		BufferedOutputStream outputStream = (BufferedOutputStream) future.getOutputStream();
		
		byte[] array = outputStream.toByteArray();
		
		return new MapByteMessage(messageID,queueName,map,array);
	}
}
